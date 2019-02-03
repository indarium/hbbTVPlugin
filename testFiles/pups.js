(function () {
    var e = "", f = "", g = function (a) {
        for (var c = document.cookie.split(";"), b = 0; b < c.length; b++) {
            var d = c[b].substr(0, c[b].indexOf("=")), e = c[b].substr(c[b].indexOf("=") + 1),
                d = d.replace(/^\s+|\s+$/g, "");
            if (d == a) return unescape(e)
        }
        return ""
    }, h = function (a, c, b) {
        a = a.split(b);
        b = "";
        for (var d = 0; d < a.length && !(b = a[d].split("=")[0] == c ? a[d].split("=")[1] : ""); d++) ;
        return b
    }, k = function (a) {
        try {
            return decodeURIComponent(a)
        } catch (c) {
            return unescape(a)
        }
    };
    (function () {
        e = h(k(document.location.hash.substring(1)), "url", "|");
        f = h(k(document.location.hash.substring(1)), "id", "|");
        var a;
        a = g("wt3_eid").split(";");
        for (var c = [], b = 0; b < a.length; b++) a[b] && c.push(a[b].substring(a[b].indexOf("|") + 1, a[b].indexOf("#")));
        a = (a = c.join(";")) ? a : g("wt_eid");
        e && window.parent.postMessage(JSON.stringify({id: f, eid: a}), e)
    })()
})();
