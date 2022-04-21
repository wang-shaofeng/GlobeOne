javascript: (function () {
 setTimeout(function(){
  console.info('start sending balance inquiry');
   console.info('start sending balance inquiry');
       EMUI.smsSendAndSaveController.showNewSendPage();
       $('#sms_send_user_input').val('222');
       $('#sms_current_content').val('BAL');
       EMUI.smsSendAndSaveController.sendMessage();
       setTimeout(function () {
               console.info('Done sending balance inquiry');
               GlobeAtHome.onFinishBalanceInquiry()
           }, 8000);
  }, 1000);

})()