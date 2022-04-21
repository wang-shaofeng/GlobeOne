javascript: (function () {
    console.info('start sending balance inquiry');
    document.getElementById('message').click();
    setTimeout(function () {
        console.log('Start populating message dialog for balance inq');
        document.getElementById('recipients_number').value = '222';
        document.getElementById('message_content').value = 'BAL';
        document.getElementById('pop_send').click();
        setTimeout(function () {
            console.info('Done sending balance inquiry');
            document.getElementById('pop_OK').click();
            GlobeAtHome.onFinishBalanceInquiry()
        }, 8000);
    }, 2000);
})()