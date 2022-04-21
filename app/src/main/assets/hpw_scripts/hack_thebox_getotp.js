javascript: (function () {
    function getOTP(second, millisCtr) {
        console.log('Start getting otp...');
        setTimeout(function () {
            if (document.getElementById('smslist-table') != null) {
                var table = document.getElementById('smslist-table').children;
                var message;
                for (var i = 0; i < table.length; i++) {
                    var regex = new RegExp("Your.*One-time.*PIN.*is.*[0-9][0-9][0-9][0-9][0-9][0-9]");
                    console.info(regex.exec(table[i].getElementsByTagName('td')[2].outerText));
                    if (regex.exec(table[i].getElementsByTagName('td')[2].outerText) != null) {
                        var message = table[i].getElementsByTagName('td')[2].outerText.trim();
                        console.info(message);
                        break;
                    }
                }
                if (message != null) {
                    console.log('Done getting otp...');
                    GlobeAtHome.passOtp(message.substr(message.length - 6))
                } else if (millisCtr < HACK_VERIF_RESULT_TIMEOUT_HACK) {
                    millisCtr += 1000;
                    getOTP(second, millisCtr);
                } else {
                    console.log('Mismatched, will end...');
                    GlobeAtHome.onFailedToConnect()
                }
            } else {
                getOTP(second, millisCtr);
            }
        }, second + 200);
    }
    getOTP(1000, 0);
})()