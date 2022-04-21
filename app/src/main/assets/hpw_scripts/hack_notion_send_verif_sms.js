javascript: (function () {
    console.log(':: SEND VERIF ::');

    var PAGE_VERIF = false;

    function onCheckVerificationResults(transactionId, millisCtr) {
        setTimeout(function () {
            if (PAGE_VERIF == false){
                console.log('elapsed time: ' + millisCtr);
                var info = document.getElementById('smsListInfo');

                if (typeof(info) != "undefined" && info != null) {
                    var currentBody = info.outerText;

                    if (currentBody.includes(transactionId)) {
                        var list = info.children;

                        for (var i = 0; i < list.length; i++) {
                            var regex = new RegExp('.*' + transactionId + '.*');
                            var message = list[i].getElementsByTagName('td')[1].outerText;
                            console.info(message);

                            if (message != null && regex.exec(message) != null) {
                                var code = message;
                                console.info(code);
                                break;
                            }
                        }

                        if (code != null) {
                            console.log('Matched!');
                            PAGE_VERIF = true;
                            GlobeAtHome.onGetVerificationResult(code)
                        } else if (millisCtr < HACK_VERIF_RESULT_TIMEOUT_HACK) {
                            console.log('Mismatched, will continue...');
                            recheckVerification(transactionId, millisCtr);
                        } else {
                            console.log('Mismatched, will end...');
                            GlobeAtHome.onFailedToConnect()
                        }
                    } else {
                        if (millisCtr < HACK_VERIF_RESULT_TIMEOUT_HACK) {
                            console.log('Skip fetching...');
                            recheckVerification(transactionId, millisCtr);
                        } else {
                            GlobeAtHome.onFailedToConnect()
                        }
                    }
                } else {
                    recheckVerification(initialContent, transactionId, millisCtr);
                }
            }
        }, 1000);
    }

    function recheckVerification(transactionId, millisCtr){
        console.log(':: recheckVerification ::');

        var btnNew = document.getElementById('lt_sms_btnNew');
        var btnCancel = document.getElementById('lt_sms_btnCancel');
        var smsList = document.getElementById('divSmsList');

        if (smsList.style.display == "block") {
            btnNew.click();
        }

        setTimeout(function () {
            if (smsList.style.display == "none") {
                btnCancel.click();
                millisCtr += 1000;
                onCheckVerificationResults(transactionId, millisCtr);
            } else {
                recheckVerification(transactionId, millisCtr);
            }
        }, 1000);
    }

    function popupClick(seconds) {
        console.log(':: hasPopup ::');

        setTimeout(function () {
            var btnSkip = document.getElementById('lt_btnSkip');
            var btnModalOk = document.getElementById('btnModalOk');

            if (typeof(btnSkip) != "undefined" && btnSkip != null) {
                btnSkip.click();
                verifyOTP();
            }

            if (typeof(btnModalOk) != "undefined" && btnModalOk != null) {
                btnModalOk.click();
                verifyOTP();
            }
        }, seconds);
    }

    function verifyOTP(){
        console.log(':: verifyOTP ::');

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
                    to.value = '21581782';
                    msg.value = 'HACK_msgBody_HACK';
                    send.click();
                    startCheck();
                }, 2000);
            } else {
                var btnNew = document.getElementById('lt_sms_btnNew');

                if (typeof(btnNew) != "undefined" && btnNew != null) {
                    btnNew.click();
                    verifyOTP();
                } else {
                    var btnSms = document.getElementById('5');
                    btnSms.click();

                    setTimeout(function() {
                        window.location.hash = "verif";
                    }, 1000);
                }
            }
        }
    }

    verifyOTP();

    function clickNew(){
        console.log('clickNew');
        var btnNew = document.getElementById('lt_sms_btnNew');
        var smsList = document.getElementById('divSmsList');

        if (typeof(btnNew) != "undefined" && btnNew != null && smsList.style.display == "block") {
            btnNew.click();
            setTimeout(function() {
                clickCancel();
            }, 1000);

        } else {
            clickCancel();
        }
    }

    function clickCancel(){
        console.log('clickCancel');

        var btnCancel = document.getElementById('lt_sms_btnCancel');
        var smsList = document.getElementById('divSmsList');

        if (typeof(btnCancel) != "undefined" && btnCancel != null && smsList.style.display == "none") {
            btnCancel.click();
        }
    }

    function startCheck(){
        console.log('startCheck');
        clickNew();

        setTimeout(function() {
            onCheckVerificationResults('HACK_transactionId_HACK', 0);
        }, 2000);
    }
})()