javascript: (function () {
    function sendSms(second) {
        console.log('Start balance inquiry');
        setTimeout(function () {
            console.info('ulchoices ' + document.getElementsByClassName("chosen-choices")[0]);
            if (document.getElementsByClassName("chosen-choices")[0] != null) {
                var ulchoices = document.getElementsByClassName("chosen-choices")[0];
                var lichoice = document.createElement("li");
                console.info('ulchoices ' + ulchoices);
                console.info('lichoice ' + lichoice);
                lichoice.appendChild(document.createTextNode("222"));
                lichoice.setAttribute("class", "search-choice");
                ulchoices.appendChild(lichoice);
                console.info('sending');
                document.getElementById('chat-input').value = "BAL";
                sendSmsClickHandler();
                console.info('sent');
                chatCancelClickHandler();
                GlobeAtHome.onFinishBalanceInquiry()
            } else {
                sendSms(second)
            }
        }, second + 200)
    }
    sendSms(1000);
})()