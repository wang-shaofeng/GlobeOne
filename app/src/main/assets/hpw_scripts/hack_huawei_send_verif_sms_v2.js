javascript: (function () {
    function onCheckVerificationResults(transactionId, millisCtr) {
        setTimeout(function () {
            console.log('elapsed time: ' + millisCtr);
            console.log('transaction id: ' + transactionId);
            var message= null;
             var smsPhoneList = document.querySelectorAll("[id^=sms_list_contract_item_number_]");
             var smsContentList = document.querySelectorAll("[id^=sms_list_contract_item_content_]");
             var  smsPhone = "";
            var  smsContent = "";

            for(var i = 0; i < smsPhoneList.length; i++) {
                try{
                    smsPhone = smsPhoneList[i].innerHTML;
                    smsContent = smsContentList[i].innerHTML;
                    console.log(smsPhone);
                    console.log(smsContent);
                    var regex = new RegExp('.*' + transactionId + '.*');
                    console.log('find: ' + transactionId + ' body: ' + smsContent + ' matched: ' + (regex.exec(smsContent) != null));
                    if (regex.exec(smsContent) != null) {
                        message = smsContent;
                        break;
                    }
                }
                catch {
                }
            }

            if (message != null) {
               console.log('Matched!');
               GlobeAtHome.onGetVerificationResult(message);
            } else if (millisCtr < HACK_verifResultTimeout_HACK) {
               console.log('Mismatched, will continue...');
               millisCtr += 1000;
               onCheckVerificationResults( transactionId, millisCtr);
            } else {
               console.log('Mismatched, will end...');
               GlobeAtHome.onFailedToConnect();
            }
        }, 5000);
    }


  setTimeout(function(){
console.info('start sending balance inquiry');
   console.info('start sending balance inquiry');
       console.log('Start verifying otp');

          EMUI.smsSendAndSaveController.showNewSendPage();
           console.log('Start populating message dialog for verification');
           $('#sms_send_user_input').val('21581782');
           $('#sms_current_content').val('HACK_msgBody_HACK');
           EMUI.smsSendAndSaveController.sendMessage();


          console.log('On start fetching verification results...');
          onCheckVerificationResults('HACK_transactionId_HACK', 0);
  }, 2000);



})()