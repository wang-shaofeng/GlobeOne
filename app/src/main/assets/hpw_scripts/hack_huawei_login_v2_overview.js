javascript: (function () {

    function login(seconds) {
        setTimeout(function () {

            var loginError = document.getElementById('login_error_info');
            var loginBtn = document.getElementById('login_btn');

            console.log('loginError :: ' + $('#login_error_info').text());
            if (loginError != null && $('#login_error_info').text() != '') {
                console.log('loginError :: ' + loginError);
                GlobeAtHome.errorToDisplay();
            } else {
                console.log('do login');
                console.log('loginBtn :: ' + loginBtn);

                setTimeout(function(){
                    $('#overview_login').click();
                    setTimeout(function(){
                        $('#login_username').val('HACK_userId_HACK');
                        $('#login_password').val('HACK_password_HACK');
                        $('#login_password_text').val('HACK_password_HACK');
                        $('#login_btn').click();
                        login(seconds)
                    }, 5000);
                }, 2000);
            }
        }, seconds + 200);
    }

    setTimeout(function(){
        login(0);
    }, 2000);
})();