"use strict";

angular.module('demoAppModule').controller('AandVCtrl', function($http, $uibModalInstance, $uibModal, apiBaseURL) {
    const AandVModal = this;


    AandVModal.form = {};
    AandVModal.formError = false;
    console.log("Hello Man")

    /** Validate and create an IOU. */
    AandVModal.create = () => {
        if (!validFormInput()) {
            AandVModal.formError = false;
        } else {
            AandVModal.formError = true

            console.log("Hello");
            const TApageurl =window.location.href.split(":")[0]+"://"+window.location.href.split("/")[2].split(":")[0]+":10007/api/iou/" ;
            const nav = document.getElementById("HKIV01NavValue").innerHTML+","+document.getElementById("DBKS01NavValue").innerHTML+","+document.getElementById("DBKS02NavValue").innerHTML+","+document.getElementById("LUKT01NavValue").innerHTML;
            $uibModalInstance.close();

            // We define the IOU creation endpoint.
            const AnadVEndpoint =
                apiBaseURL +
                `issue-nav?fundId='HKIV01,DBKS01,DBKS02,LUKT01'&nav=${nav}`;

            // We hit the endpoint to create the IOU and handle success/failure responses.
            $http.get(AnadVEndpoint).then(
               $http.get(TApageurl+`updateNav?fundid=HKIV01,DBKS01,DBKS02,LUKT01&navValue=${nav}`)
                //(result) => createIOUModal.displayMessage(result)
            );
        }
    };

    /** Displays the success/failure response from attempting to create an IOU. */
    AandVModal.displayMessage = (message) => {
    console.log(message)
        const AandVIOUMsgModal = $uibModal.open({
            templateUrl: 'AandVMsgModal.html',
            controller: 'AandVMsgModalCtrl',
            controllerAs: 'AandVMsgModal',
            resolve: {
                message: () => message
            }
        });

        // No behaviour on close / dismiss.
        AandVModal.result.then(() => {}, () => { location.reload();}, () => { location.reload();});
        // createIOUMsgModal.result.then(() => {}, () => { demoApp.refresh();}, () => { demoApp.refresh();});
    };

    /** Closes the IOU creation modal. */
    AandVModal.cancel = () => $uibModalInstance.dismiss();

    // Validates the IOU.
    function validFormInput() {
        return true;
    }
});

// Controller for the success/fail modal.
angular.module('demoAppModule').controller('AandVMsgModalCtrl', function($uibModalInstance, message) {
    const AandVMsgModal = this;
    AandVMsgModal.message = message.data;
});