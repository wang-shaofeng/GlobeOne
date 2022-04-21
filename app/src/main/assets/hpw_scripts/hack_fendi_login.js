javascript: (function () {
    function login(seconds) {
        setTimeout(function () {
            var loginBtn = document.getElementById('btnLogin');
            if (loginBtn != null) {
                var username = document.getElementById('txtUsr');
                username.value = 'HACK_mUsername_HACK';
                var password = document.getElementById('txtPwd');
                password.value = 'HACK_mPassword_HACK';
                loginBtn.click();
            } else {
                var logoutSpan = document.getElementById('logout');
                if (logoutSpan != null) {
                    if (logoutSpan.firstElementChild.getAttribute('style') == '') {} else {
                        login(seconds);
                    }
                } else {
                    login(seconds);
                }
            }
        }, seconds + 200);
    }
    login(0);
})()