package com.fastaccess.data.dao.wiki

data class FirebaseWikiConfigModel(
    var wikiWrapper: String = "#wiki-wrapper",
    var wikiHeader: String = ".gh-header > h1.gh-header-title",
    var sideBarHref: String = "href",
    var wikiBody: String = "#wiki-body",
    var wikiSubHeader: String = ".gh-header-meta",
    var wikiContent: String = "#wiki-content",
    var sideBarGroup: String = ".js-wiki-sidebar-toggle-display > ul > li",
    var sideBarGroupSummaryTitle: String = "details > summary > div > a",
    var sideBarGroupItem: String = "details > ul > li",
)