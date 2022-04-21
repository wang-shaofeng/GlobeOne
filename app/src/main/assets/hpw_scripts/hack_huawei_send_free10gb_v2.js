javascript: (function () {
  setTimeout(function(){
  console.info('start sending balance inquiry');
    EMUI.smsSendAndSaveController.showNewSendPage();
    $('#sms_send_user_input').val('8080');
    $('#sms_current_content').val('FREE10GB');
    EMUI.smsSendAndSaveController.sendMessage();
    setTimeout(function () {
            console.info('Done sending balance inquiry');
           GlobeAtHome.onFinishSendFree10GBSms()
        }, 3000);
  }, 1000);


})()


