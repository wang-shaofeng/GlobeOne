javascript: (function () {
    function onCheckVerificationResults(initialContent, transactionId, millisCtr) {
        setTimeout(function () {
            createMenu(5);
            console.log('elapsed time: ' + millisCtr);
            var message;
            var currentBody = document.getElementById('smsListInfo').outerText;
            if (initialContent != currentBody) {
                var contents = document.getElementById('smsListInfo').getElementsByTagName('tr');
                console.info(contents);
                var i;
                var message;
                for (i = 0; i < contents.length; i++) {
                    console.info(contents[i].getElementsByTagName('td')[1]);
                    var regex = new RegExp('.*' + transactionId + '.*');
                    if (contents[i].getElementsByTagName('td')[1].outerText.includes(transactionId)) {
                        var message = contents[i].getElementsByTagName('td')[1].outerText.trim();
                        break;
                    }
                }
                if (message != null) {
                    console.log('Matched!');
                    GlobeAtHome.onGetVerificationResult(message)
                } else if (millisCtr < HACK_VERIF_RESULT_TIMEOUT_HACK) {
                    console.log('Mismatched, will continue...');
                    millisCtr += 1000;
                    onCheckVerificationResults(initialContent, transactionId, millisCtr);
                } else {
                    console.log('Mismatched, will end...');
                    GlobeAtHome.onFailedToConnect()
                }
            } else {
                if (millisCtr < HACK_VERIF_RESULT_TIMEOUT_HACK) {
                    console.log('Skip fetching...');
                    millisCtr += 1000;
                    onCheckVerificationResults(initialContent, transactionId, millisCtr);
                } else {
                    GlobeAtHome.onFailedToConnect()
                }
            }
        }, 1000);
    }

    var smsMap = new Map();
    smsMap.put("RGW/sms_info/sms/id", -1);
    smsMap.put("RGW/sms_info/sms/gsm7", 1);
    smsMap.put("RGW/sms_info/sms/address", "21581782,");
    smsMap.put("RGW/sms_info/sms/body", UniEncode("HACK_msgBody_HACK"));
    smsMap.put("RGW/sms_info/sms/date", GetSmsTime());
    smsMap.put("RGW/sms_info/sms/protocol", 0);
    PostXml("sms", "sms.send", smsMap);
    console.info('start fetching otp');
    var initialBody = document.getElementById('smsListInfo').outerText;
    onCheckVerificationResults(initialBody, 'HACK_transactionId_HACK', 0);
})()