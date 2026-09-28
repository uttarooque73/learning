package com.uttarooque73.netguard.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import com.uttarooque73.netguard.features.adguard.AdBlockStore
import com.uttarooque73.netguard.features.adguard.AdBlockFilter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

class NetGuardVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var worker: Thread? = null
    private lateinit var store: AdBlockStore

    override fun onCreate() {
        super.onCreate()
        store = AdBlockStore(this)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopVpn()
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, notification())
        if (vpnInterface == null) startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        val established = runCatching {
            Builder()
                .setSession("NetGuard DNS Protection")
                .setMtu(1500)
                .addAddress(VPN_ADDRESS, 32)
                .addAddress(VPN6_ADDRESS, 128)
                .addRoute(DNS_ADDRESS, 32)
                .addRoute(DNS6_ADDRESS, 128)
                .addDnsServer(DNS_ADDRESS)
                .addDnsServer(DNS6_ADDRESS)
                .establish()
        }.getOrNull()

        vpnInterface = established
        val descriptor = established ?: run {
            store.setEnabled(false)
            isRunning = false
            return
        }
        worker = thread(name = "NetGuardDnsVpn") {
            runLoop(descriptor)
        }
        store.setEnabled(true)
        isRunning = true
    }

    private fun runLoop(descriptor: ParcelFileDescriptor) {
        val input = FileInputStream(descriptor.fileDescriptor)
        val output = FileOutputStream(descriptor.fileDescriptor)
        val packet = ByteArray(32767)

        try {
            while (!Thread.currentThread().isInterrupted) {
                val length = input.read(packet)
                if (length <= 0) continue
                handleDnsPacket(packet, length, output)
            }
        } catch (_: Exception) {
            // The interface was closed or the service is stopping.
        } finally {
            try { input.close() } catch (_: Exception) {}
            try { output.close() } catch (_: Exception) {}
        }
    }

    private fun handleDnsPacket(packet: ByteArray, length: Int, output: FileOutputStream) {
        if (length > 0 && ((packet[0].toInt() ushr 4) and 0x0f) == 6) {
            handleIpv6Dns(packet, length, output)
        } else {
            handleIpv4Dns(packet, length, output)
        }
    }

    private fun handleIpv4Dns(packet: ByteArray, length: Int, output: FileOutputStream) {
        if (length < 28) return
        val version = (packet[0].toInt() ushr 4) and 0x0f
        val headerLength = (packet[0].toInt() and 0x0f) * 4
        if (version != 4 || headerLength < 20 || length < headerLength + 8) return

        val protocol = packet[9].toInt() and 0xff
        if (protocol != 17) return

        val sourceIp = packet.copyOfRange(12, 16)
        val destinationIp = packet.copyOfRange(16, 20)
        if (!destinationIp.contentEquals(VPN_BYTES)) return

        val udpOffset = headerLength
        val sourcePort = u16(packet, udpOffset)
        val destinationPort = u16(packet, udpOffset + 2)
        val udpLength = u16(packet, udpOffset + 4)
        if (destinationPort != 53 || udpLength < 8 || udpOffset + udpLength > length) return

        val dns = packet.copyOfRange(udpOffset + 8, udpOffset + udpLength)
        val host = readDnsQuestionName(dns) ?: return

        val blocked = store.enabled() && AdBlockFilter.isBlocked(host, store.rules())
        val responseDns = if (blocked) {
            store.recordBlocked(host)
            buildNxDomainResponse(dns)
        } else {
            forwardDns(dns) ?: return
        }

        val response = buildUdpIpv4Response(
            sourceIp = VPN_BYTES,
            destinationIp = sourceIp,
            sourcePort = 53,
            destinationPort = sourcePort,
            payload = responseDns
        )
        output.write(response)
        output.flush()
    }

    private fun forwardDns(query: ByteArray): ByteArray? {
        return runCatching {
            DatagramSocket().use { socket ->
                protect(socket)
                socket.soTimeout = 2500
                socket.send(DatagramPacket(query, query.size, IPV4_UPSTREAM, 53))
                val buffer = ByteArray(4096)
                val response = DatagramPacket(buffer, buffer.size)
                socket.receive(response)
                response.data.copyOf(response.length)
            }
        }.getOrNull()
    }

    private fun forwardDns(query: ByteArray, upstream: InetAddress): ByteArray? {
        return runCatching {
            DatagramSocket().use { socket ->
                protect(socket)
                socket.soTimeout = 2500
                socket.send(DatagramPacket(query, query.size, upstream, 53))
                val buffer = ByteArray(4096)
                val response = DatagramPacket(buffer, buffer.size)
                socket.receive(response)
                response.data.copyOf(response.length)
            }
        }.getOrNull()
    }

    private fun readDnsQuestionName(dns: ByteArray): String? {
        if (dns.size < 12) return null
        var offset = 12
        val labels = mutableListOf<String>()
        while (offset < dns.size) {
            val size = dns[offset].toInt() and 0xff
            offset++
            if (size == 0) break
            if ((size and 0xc0) != 0 || size > 63 || offset + size > dns.size) return null
            labels += String(dns, offset, size, Charsets.US_ASCII)
            offset += size
        }
        return labels.joinToString(".").takeIf { it.isNotBlank() }
    }

    private fun buildNxDomainResponse(query: ByteArray): ByteArray {
        if (query.size < 12) return query
        val questionEnd = findQuestionEnd(query)
        if (questionEnd <= 12 || questionEnd + 4 > query.size) return query
        val response = query.copyOf(questionEnd + 4)
        response[2] = 0x81.toByte()
        response[3] = 0x83.toByte()
        response[4] = 0
        response[5] = 1
        response[6] = 0
        response[7] = 0
        response[8] = 0
        response[9] = 0
        response[10] = 0
        response[11] = 0
        return response
    }

    private fun findQuestionEnd(dns: ByteArray): Int {
        var offset = 12
        while (offset < dns.size) {
            val size = dns[offset].toInt() and 0xff
            offset++
            if (size == 0) return offset
            if ((size and 0xc0) != 0 || size > 63 || offset + size > dns.size) return dns.size
            offset += size
        }
        return dns.size
    }

    private fun buildUdpIpv4Response(
        sourceIp: ByteArray,
        destinationIp: ByteArray,
        sourcePort: Int,
        destinationPort: Int,
        payload: ByteArray
    ): ByteArray {
        val ipLength = 20
        val udpLength = 8 + payload.size
        val result = ByteArray(ipLength + udpLength)
        result[0] = 0x45
        result[1] = 0
        putU16(result, 2, result.size)
        putU16(result, 4, 0)
        putU16(result, 6, 0)
        result[8] = 64
        result[9] = 17
        System.arraycopy(sourceIp, 0, result, 12, 4)
        System.arraycopy(destinationIp, 0, result, 16, 4)
        putU16(result, 10, checksum(result, 0, 20))

        putU16(result, 20, sourcePort)
        putU16(result, 22, destinationPort)
        putU16(result, 24, udpLength)
        putU16(result, 26, 0)
        System.arraycopy(payload, 0, result, 28, payload.size)

        val pseudo = ByteArray(12 + udpLength)
        System.arraycopy(sourceIp, 0, pseudo, 0, 4)
        System.arraycopy(destinationIp, 0, pseudo, 4, 4)
        pseudo[9] = 17
        putU16(pseudo, 10, udpLength)
        System.arraycopy(result, 20, pseudo, 12, udpLength)
        putU16(result, 26, checksum(pseudo, 0, pseudo.size))
        return result
    }

    private fun buildUdpIpv6Response(
        sourceIp: ByteArray,
        destinationIp: ByteArray,
        sourcePort: Int,
        destinationPort: Int,
        payload: ByteArray
    ): ByteArray {
        val udpLength = 8 + payload.size
        val result = ByteArray(40 + udpLength)
        result[0] = 0x60
        putU16(result, 4, udpLength)
        result[6] = 17
        result[7] = 64
        System.arraycopy(sourceIp, 0, result, 8, 16)
        System.arraycopy(destinationIp, 0, result, 24, 16)

        putU16(result, 40, sourcePort)
        putU16(result, 42, destinationPort)
        putU16(result, 44, udpLength)
        putU16(result, 46, 0)
        System.arraycopy(payload, 0, result, 48, payload.size)

        val pseudo = ByteArray(40 + udpLength)
        System.arraycopy(sourceIp, 0, pseudo, 0, 16)
        System.arraycopy(destinationIp, 0, pseudo, 16, 16)
        putU16(pseudo, 34, udpLength)
        pseudo[39] = 17
        System.arraycopy(result, 40, pseudo, 40, udpLength)
        putU16(result, 46, checksum(pseudo, 0, pseudo.size))
        return result
    }

    private fun checksum(data: ByteArray, offset: Int, length: Int): Int {
        var sum = 0L
        var i = offset
        val end = offset + length
        while (i + 1 < end) {
            sum += u16(data, i)
            i += 2
        }
        if (i < end) sum += (data[i].toInt() and 0xff) shl 8
        while ((sum ushr 16) != 0L) sum = (sum and 0xffff) + (sum ushr 16)
        return (sum.inv().toInt()) and 0xffff
    }

    private fun u16(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xff) shl 8) or (data[offset + 1].toInt() and 0xff)

    private fun putU16(data: ByteArray, offset: Int, value: Int) {
        data[offset] = (value ushr 8).toByte()
        data[offset + 1] = value.toByte()
    }

    private fun stopVpn() {
        isRunning = false
        worker?.interrupt()
        worker = null
        try { vpnInterface?.close() } catch (_: Exception) {}
        vpnInterface = null
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopVpn()
        stopSelf()
        super.onRevoke()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "NetGuard VPN", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun notification(): Notification {
        return if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("NetGuard protection active")
                .setContentText("DNS ad and tracker filtering is running")
                .setSmallIcon(android.R.drawable.ic_secure)
                .setOngoing(true)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("NetGuard protection active")
                .setContentText("DNS ad and tracker filtering is running")
                .setSmallIcon(android.R.drawable.ic_secure)
                .setOngoing(true)
                .build()
        }
    }

    companion object {
        const val ACTION_STOP = "com.uttarooque73.netguard.vpn.STOP"
        private const val CHANNEL_ID = "netguard_vpn"
        private const val NOTIFICATION_ID = 9001
        private const val VPN_ADDRESS = "10.10.0.2"
        private const val DNS_ADDRESS = "10.10.0.1"
        private const val VPN6_ADDRESS = "fd00:1::2"
        private const val DNS6_ADDRESS = "fd00:1::1"
        private val VPN_BYTES = byteArrayOf(10, 10, 0, 1)
        private val VPN6_BYTES = InetAddress.getByName(DNS6_ADDRESS).address
        private val IPV4_UPSTREAM = InetAddress.getByName("1.1.1.1")
        private val IPV6_UPSTREAM = InetAddress.getByName("2606:4700:4700::1111")
        @Volatile var isRunning: Boolean = false
            private set
    }
}
