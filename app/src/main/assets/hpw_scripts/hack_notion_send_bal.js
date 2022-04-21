javascript: (function() {
     console.log('SMS');

     var PAGE_BAL = false;

    function popupClick(seconds) {
        console.log(':: hasPopup ::');

        setTimeout(function () {
            var btnSkip = document.getElementById('lt_btnSkip');
            var btnModalOk = document.getElementById('btnModalOk');

            if (typeof(btnSkip) != "undefined" && btnSkip != null) {
                btnSkip.click();
                sendBalance(0);
            }

            if (typeof(btnModalOk) != "undefined" && btnModalOk != null) {
                btnModalOk.click();
                sendBalance(0);
            }
        }, seconds);
    }

    function sendBalance(second){
        console.log(':: sendBalance ::');

        var ol = document.getElementById('ol');

        if (typeof(ol) != "undefined" && ol != null && ol.style.display == "block") {
            popupClick(0);
        } else {
            setTimeout(function () {
                if (PAGE_BAL == false) {
                    var to = document.getElementById('txtNumberList');
                    var msg = document.getElementById('txtSmsContent');
                    var send = document.getElementById('lt_sms_btnSend');

                    if (typeof(to) != "undefined" && to != null &&
                        typeof(msg) != "undefined" && msg != null &&
                        typeof(send) != "undefined" && send != null) {

                        PAGE_BAL = true;
                        to.value = '222';
                        msg.value = 'BAL';
                        send.click();

                        setTimeout(function() {
                            window.location.hash = "otp";
                        }, 1000);
                    } else {
                        var btnNew = document.getElementById('lt_sms_btnNew');

                        if (typeof(btnNew) != "undefined" && btnNew != null) {
                            btnNew.click();
                        }

                        sendBalance(second);
                    }
                }
            }, second + 200);
        }
    }

    sendBalance(0);
})()