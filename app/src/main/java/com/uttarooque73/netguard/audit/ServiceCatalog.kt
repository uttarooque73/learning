package com.uttarooque73.netguard.audit

object ServiceCatalog {
    val ports: List<Pair<Int, String>> = listOf(
        22 to "SSH",
        23 to "Telnet",
        53 to "DNS",
        80 to "HTTP",
        443 to "HTTPS",
        445 to "SMB",
        8080 to "HTTP-alt",
        8443 to "HTTPS-alt"
    )
}
