javascript: (function() {
    console.log('LOGIN');

    var PAGE_LOGIN = false;
    var PAGE_BAL = false;

    function popupClick(seconds) {
        console.log(':: hasPopup ::');

        setTimeout(function () {
            var btnSkip = document.getElementById('lt_btnSkip');
            var btnModalOk = document.getElementById('btnModalOk');

            if (typeof(btnSkip) != "undefined" && btnSkip != null) {
                btnSkip.click();
                smsClick();
            }

            if (typeof(btnModalOk) != "undefined" && btnModalOk != null) {
                btnModalOk.click();
                smsClick();
            }
        }, seconds);
    }

    function smsClick() {
        console.log(':: smsClick ::');

        var ol = document.getElementById('ol');

        if (typeof(ol) != "undefined" && ol != null && ol.style.display == "block") {
            popupClick(0);
        } else {
            if (PAGE_BAL == false) {
                var btnNew = document.getElementById('lt_sms_btnNew');
                var btnSms = document.getElementById('5');

                if (typeof(btnSms) != "undefined" && btnSms != null) {
                    if (typeof(btnNew) != "undefined" && btnNew != null) {
                        PAGE_BAL = true;
                        btnNew.click();
                        window.location.hash = "bal";
                    } else {
                        btnSms.click();
                        smsClick();
                    }
                } else {
                    smsClick();
                }
            }
        }
    }

    function login(second) {
        console.log(':: login ::');

        setTimeout(function () {
            if (PAGE_LOGIN == false) {
                var ol = document.getElementById('ol');
                var loginBtn = document.getElementById('btnSignIn');
                var username = document.getElementById('tbarouter_username');
                var password = document.getElementById('tbarouter_password');

                if (typeof(ol) != "undefined" && ol != null && ol.style.display == "block" &&
                    typeof(loginBtn) != "undefined" && loginBtn != null &&
                    typeof(username) != "undefined" && username != null &&
                    typeof(password) != "undefined" && password != null) {

                    PAGE_LOGIN = true;
                    username.value = 'HACK_mUsername_HACK';
                    password.value = 'HACK_mPassword_HACK';
                    loginBtn.click();

                    setTimeout(function() {
                        smsClick();
                    }, 1000);
                } else {
                    window.location.hash = "";
                }
            }
        }, second + 200);
    }

    login(0);
})()