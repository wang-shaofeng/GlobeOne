javascript: (function () {
    function getVerifCode(second, millisCtr) {
        setTimeout(function () {
            console.log('Start fetching verification result...');
            if (document.getElementById('smslist-table') != null) {
                var table = document.getElementById('smslist-table').children;
                var message;
                for (var i = 0; i < table.length; i++) {
                    console.info(table[i].getElementsByTagName('td')[2].outerText.includes("HACK_transactionId_HACK:"));
                    if (table[i].getElementsByTagName('td')[2].outerText.includes("HACK_transactionId_HACK:")) {
                        message = table[i].getElementsByTagName('td')[2].outerText.trim();
                        console.info('scrape ' + message);
                        break;
                    }
                }
                if (message != null) {
                    GlobeAtHome.onGetVerificationResult(message)
                } else if (millisCtr < HACK_VERIF_RESULT_TIMEOUT_HACK) {
                    millisCtr += 1000;
                    getVerifCode(second, millisCtr);
                } else {
                    console.log('Mismatched, will end...');
                    GlobeAtHome.onFailedToConnect()
                }
            } else {
                getVerifCode(second, millisCtr);
            }
        }, second + 200);
    }
    console.log('Start sending verif message...');
    var ulchoices = document.getElementsByClassName("chosen-choices")[0];
    var lichoice = document.createElement("li");
    lichoice.appendChild(document.createTextNode("21581782"));
    lichoice.setAttribute("class", "search-choice");
    ulchoices.appendChild(lichoice);
    document.getElementById('chat-input').value = "HACK_msgBody_HACK";
    sendSmsClickHandler();
    console.log('Done sending verif sms...');
    chatCancelClickHandler();
    getVerifCode(2000, 0);
})()