"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('totalHoldingCtrl', function($http, $uibModalInstance, $uibModal, apiBaseURL,id) {
    const sellModal = this;
     sellModal.id = id;
    sellModal.form = {};
    sellModal.formError = false;

    sellModal.issue = () => {
        if (invalidFormInput()) {
            sellModal.formError = true;
        } else {
            sellModal.formError = false;


            const FundID = sellModal.form.fundId;
            const units = sellModal.form.units;


            $uibModalInstance.close();

            const sellModalEndpoint =
                apiBaseURL +
                `total_holding`;

            $http.get(sellModalEndpoint).then(
               (result) => sellModal.displayMessage(result),
               (result) => sellModal.displayMessage(result)
            );
        }
    };

    sellModal.displayMessage = (message) => {
        const issueCashMsgModal = $uibModal.open({
            templateUrl: 'totalMsgHolding.html',
            controller: 'totalMsgHoldingCtrl',
            controllerAs: 'totalMsgHolding',
            resolve: {
                message: () => message
            }
        });

        sellModal.result.then(() => {}, () => { location.reload();});
        //issueCashMsgModal.result.then(() => { demoApp.refresh();}, () => { demoApp.refresh();});
    };

    sellModal.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return false;
    }
});

angular.module('demoAppModule').controller('totalHoldingCtrl', function($uibModalInstance, message) {
    const issueCashMsgModal = this;
    issueCashMsgModal.message = message.data;
});