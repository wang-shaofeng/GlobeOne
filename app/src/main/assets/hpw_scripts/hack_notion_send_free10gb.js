javascript: (function() {
    console.log('FREE10GB');

    function popupClick(seconds, millisCtr) {
        console.log(':: hasPopup ::');

        setTimeout(function () {
            var btnSkip = document.getElementById('lt_btnSkip');
            var btnModalOk = document.getElementById('btnModalOk');

            if (typeof(btnSkip) != "undefined" && btnSkip != null) {
                btnSkip.click();
                free10GB();
            }

            if (typeof(btnModalOk) != "undefined" && btnModalOk != null) {
                btnModalOk.click();
                free10GB();
            }
        }, 200);
    }

    function free10GB(){
        console.log(':: free10GB ::');

        var ol = document.getElementById('ol');

        if (typeof(ol) != "undefined" && ol != null && ol.style.display == "block") {
            popupClick(0);
        } else {
            var to = document.getElementById('txtNumberList');
            var msg = document.getElementById('txtSmsContent');
            var send = document.getElementById('lt_sms_btnSend');
            var smsList = document.getElementById('divSmsList');

            if (typeof(to) != "undefined" && to != null &&
                typeof(msg) != "undefined" && msg != null &&
                typeof(send) != "undefined" && send != null &&
                typeof(smsList) != "undefined" && smsList != null && smsList.style.display == "none") {

                setTimeout(function () {
                    to.value = '8080';
                    msg.value = 'FREE10GB';
                    send.click();
                    GlobeAtHome.onFinishSendFree10GBSms()
                }, 2000);
            } else if (typeof(smsList) != "undefined" && smsList != null && smsList.style.display == "block") {
                var btnNew = document.getElementById('lt_sms_btnNew');
                btnNew.click();
                free10GB();
            } else {
                 var btnSms = document.getElementById('5');
                btnSms.click();

                setTimeout(function() {
                    window.location.hash = "free10gb";
                }, 1000);
            }
        }
    }

    free10GB();
})()