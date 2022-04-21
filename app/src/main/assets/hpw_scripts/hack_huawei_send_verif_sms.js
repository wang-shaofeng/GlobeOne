javascript: (function () {
    function onCheckVerificationResults(initialContent, transactionId, millisCtr) {
        setTimeout(function () {
            console.log('elapsed time: ' + millisCtr);
            var message;
            var currentBody = document.getElementById('sms_table').outerText;
            if (initialContent != currentBody) {
                for (var i = 0; i < g_sms_smsList.length; i++) {
                    var content = g_sms_smsList[i].Content;
                    var regex = new RegExp('.*' + transactionId + '.*');
                    console.log('find: ' + transactionId + ' body: ' + content +
                        ' matched: ' + (regex.exec(content) != null));
                    if (regex.exec(content) != null) {
                        message = content;
                        break;
                    }
                }
                if (message != null) {
                    console.log('Matched!');
                    GlobeAtHome.onGetVerificationResult(message)
                } else if (millisCtr < HACK_verifResultTimeout_HACK) {
                    console.log('Mismatched, will continue...');
                    millisCtr += 1000;
                    onCheckVerificationResults(initialContent, transactionId, millisCtr);
                } else {
                    console.log('Mismatched, will end...');
                    GlobeAtHome.onFailedToConnect()
                }
            } else {
                if (millisCtr < HACK_verifResultTimeout_HACK) {
                    console.log('Skip fetching...');
                    millisCtr += 1000;
                    onCheckVerificationResults(initialContent, transactionId, millisCtr);
                } else {
                    GlobeAtHome.onFailedToConnect()
                }
            }
        }, 1000);
    }

    console.log('Start verifying otp');
    document.getElementById('message').click();
    setTimeout(function () {
        console.log('Start populating message dialog for verification');
        document.getElementById('recipients_number').value = '21581782';
        document.getElementById('message_content').value = 'HACK_msgBody_HACK';
        document.getElementById('pop_send').click();
    }, 2000);

    console.log('On start fetching verification results...');
    var initialBody = document.getElementById('sms_table').outerText;
    onCheckVerificationResults(initialBody, 'HACK_transactionId_HACK', 0);
})()