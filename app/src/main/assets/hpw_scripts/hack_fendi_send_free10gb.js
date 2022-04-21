javascript: (function () {
    function sendSms(second) {
        setTimeout(function () {
            console.info('ulchoices ' + document.getElementsByClassName("chosen-choices")[0]);
            if (document.getElementsByClassName("chosen-choices")[0] != null) {
                var ulchoices = document.getElementsByClassName("chosen-choices")[0];
                var lichoice = document.createElement("li");
                console.info('ulchoices ' + ulchoices);
                console.info('lichoice ' + lichoice);
                lichoice.appendChild(document.createTextNode("8080"));
                lichoice.setAttribute("class", "search-choice");
                ulchoices.appendChild(lichoice);
                console.info('sending');
                document.getElementById('chat-input').value = "FREE10GB";
                sendSmsClickHandler();
                console.info('sent');
                GlobeAtHome.onFinishSendFree10GBSms()
            } else {
                sendSms(second)
            }
        }, second + 200)
    }
    sendSms(1000);
})()