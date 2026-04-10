(function () {
    function normalizeBaseUrl(value) {
        return String(value || "").trim().replace(/\/+$/, "");
    }

    function resolveApiBaseUrl() {
        var fromWindow = window.LEAVEPAL_ENV && window.LEAVEPAL_ENV.API_BASE_URL;
        var fromStorage = "";
        try {
            fromStorage = localStorage.getItem("LEAVEPAL_API_BASE_URL") || "";
        } catch (error) {
            fromStorage = "";
        }

        return normalizeBaseUrl(fromWindow || fromStorage);
    }

    var apiBaseUrl = resolveApiBaseUrl();
    var nativeFetch = window.fetch.bind(window);

    window.LEAVEPAL_API_BASE_URL = apiBaseUrl;
    window.buildApiUrl = function (path) {
        if (!path) {
            return apiBaseUrl;
        }

        var value = String(path);
        if (/^https?:\/\//i.test(value)) {
            return value;
        }

        if (!value.startsWith("/")) {
            value = "/" + value;
        }

        return apiBaseUrl + value;
    };

    window.fetch = function (input, init) {
        if (typeof input === "string" && input.startsWith("/api/")) {
            return nativeFetch(window.buildApiUrl(input), init);
        }

        if (input instanceof Request) {
            var requestUrl = input.url || "";
            if (requestUrl.startsWith("/api/")) {
                var rewritten = new Request(window.buildApiUrl(requestUrl), input);
                return nativeFetch(rewritten, init);
            }
        }

        return nativeFetch(input, init);
    };
})();
