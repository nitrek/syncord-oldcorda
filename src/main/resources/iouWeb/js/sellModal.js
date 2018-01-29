"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('sellModalCtrl', function($http, $uibModalInstance, $uibModal, apiBaseURL) {
    const sellModal = this;
    sellModal.form = {};
    sellModal.formError = false;

    sellModal.create = () => {
        if (invalidFormInput()) {
            sellModal.formError = true;
        } else {
            sellModal.formError = false;


            const FundID = sellModal.form.fundId;
            const units = sellModal.form.units;

    console.log("Hello123");
            $uibModalInstance.close();

             const sellModalEndpoint =
                            apiBaseURL +
                            `redumption-iou?fundId=${FundID}&Unit=${units}`;

                            console.log("Hello");

            $http.get(sellModalEndpoint).then(
               (result) => sellModal.displayMessage(result)
               //(result) => sellModal.displayMessage(result)
            );
        }
    };

    sellModal.displayMessage = (message) => {
        const sellMsgModal = $uibModal.open({
            templateUrl: 'sellMsgModal.html',
            controller: 'sellMsgModalCtrl',
            controllerAs: 'sellMsgModal',
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

angular.module('demoAppModule').controller('sellMsgModalCtrl', function($uibModalInstance, message) {
    const issueCashMsgModal = this;
    issueCashMsgModal.message = message.data;
});