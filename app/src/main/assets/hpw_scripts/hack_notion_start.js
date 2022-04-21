javascript: (function () {
    console.log('START');
    function login(second) {
        console.log(':: login ::');

        setTimeout(function () {
            var loginMainBtn = document.getElementById('MainLogInt');

            if (typeof(loginMainBtn) != "undefined" && loginMainBtn != null) {
                loginMainBtn.click();
            } else {
                login(second);
            }
        }, second + 200);
    }

    login(0);
})()