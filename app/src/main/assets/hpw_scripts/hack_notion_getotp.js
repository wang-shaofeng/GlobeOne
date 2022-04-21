javascript: (function () {
    console.log(':: OTP ::');

    var PAGE_OTP = false;

    function popupClick(seconds, millisCtr) {
        console.log(':: hasPopup ::');

        setTimeout(function () {
            var btnSkip = document.getElementById('lt_btnSkip');
            var btnModalOk = document.getElementById('btnModalOk');

            if (typeof(btnSkip) != "undefined" && btnSkip != null) {
                btnSkip.click();
                getOTP(seconds, millisCtr);
            }

            if (typeof(btnModalOk) != "undefined" && btnModalOk != null) {
                btnModalOk.click();
                getOTP(seconds, millisCtr);
            }
        }, 200);
    }

    function getOTP(second, millisCtr) {
        var ol = document.getElementById('ol');

        if (typeof(ol) != "undefined" && ol != null && ol.style.display == "block") {
            popupClick(0);
        } else {
            setTimeout(function () {
                if (PAGE_OTP == false) {
                    console.log(':: getOTP ::');

                    var info = document.getElementById('smsListInfo');

                    if (typeof(info) != "undefined" && info != null) {
                        var list = info.children;
                        var message;
                        for (var i = 0; i < list.length; i++) {
                            var regex = new RegExp("Your.*One-time.*PIN.*is.*[0-9][0-9][0-9][0-9][0-9][0-9]");
                            var message = list[i].getElementsByTagName('td')[1].outerText;
                            console.info('message :: ' + message);

                            if (message != null && regex.exec(message) != null) {
                                var code = message;
                                console.info("CODE :: " + code);
                                break;
                            }
                        }

                        if (code != null) {
                            PAGE_OTP = true;
                            console.log('Match...');
                            GlobeAtHome.passOtp(code.substr(code.length - 6))
                        } else if (millisCtr < HACK_VERIF_RESULT_TIMEOUT_HACK) {
                            console.log('Mismatched, will continue...');
                            millisCtr += 1000;
                            clickNew(second, millisCtr);
                        } else {
                            console.log('Mismatched, will end...');
                            GlobeAtHome.onFailedToConnect()
                        }
                    } else {
                        console.log('Restart get OTP...');
                        var btnSms = document.getElementById('5');
                        btnSms.click();

                        setTimeout(function() {
                            window.location.hash = "otp";
                        }, 1000);
                    }
                }
            }, second + 200);
        }
    }

    function clickNew(second, millisCtr){
        console.log('clickNew');
         var btnNew = document.getElementById('lt_sms_btnNew');

         if (typeof(btnNew) != "undefined" && btnNew != null) {
            btnNew.click();

            setTimeout(function() {
                clickCancel(second, millisCtr);
            }, 1000);
         } else {
            getOTP(second, millisCtr);
         }
    }

    function clickCancel(second, millisCtr){
        console.log('clickCancel');

        var btnCancel = document.getElementById('lt_sms_btnCancel');

        if (typeof(btnCancel) != "undefined" && btnCancel != null) {
             btnCancel.click();

             setTimeout(function() {
                 getOTP(second, millisCtr);
             }, 1000);
        } else {
            getOTP(second, millisCtr);
        }
    }

    getOTP(1000, 0);
})()