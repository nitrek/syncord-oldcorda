"use strict";

angular.module('demoAppModule').controller('CreateIOUModalCtrl', function($http, $uibModalInstance, $uibModal, apiBaseURL, peers, me) {
    const createIOUModal = this;

    createIOUModal.peers = peers;
    createIOUModal.form = {};
    createIOUModal.formError = false;
    createIOUModal.me = (me.split(',')[0]).split('=')[1];

    //console.log(createIOUModal.me);

    //$http.get("http://ec2-52-221-244-252.ap-southeast-1.compute.amazonaws.com:9000/kyc" ).then(
     //(result) => { console.log(result);}, (result) => { console.log(result);}
    //);
    /** Validate and create an IOU. */
    createIOUModal.create = () => {
        if (!validFormInput()) {
            createIOUModal.formError = false;
        } else {
            createIOUModal.formError = true

            const fundId = createIOUModal.form.fundId;
            const kycStatus = "Yes";//createIOUModal.form.kycStatus;
            const txType = "SUBSCRIPTION";
            const kycAPI = "http://ec2-52-221-244-252.ap-southeast-1.compute.amazonaws.com:9000/kyc"
            const transactionAmount = createIOUModal.form.amount;
            var randomNumberBetween0and19 = Math.floor(Math.random() * 3000);
            $uibModalInstance.close();

            // We define the IOU creation endpoint.
            const issueIOUEndpoint =
                apiBaseURL +
                `issue-iou?fundId=${fundId}&txType=${txType}&transactionAmount=${transactionAmount}&txid=${randomNumberBetween0and19}&kycValid=`;

            // We hit the endpoint to create the IOU and handle success/failure responses.
             $http.get(kycAPI).then((result) => {
                                    var fetchedKYC = (result.data[createIOUModal.me] == null) ? "No" : result.data[createIOUModal.me];
                                    console.log(fetchedKYC);
            						return $http.get(issueIOUEndpoint+fetchedKYC);
                                  }).then(
                                  (result) => createIOUModal.displayMessage(result)
                                  );
            //$http.get(issueIOUEndpoint).then(
              //  (result) => createIOUModal.displayMessage(result),
                //(result) => createIOUModal.displayMessage(result)
           // ));
        }
    };

    /** Displays the success/failure response from attempting to create an IOU. */
    createIOUModal.displayMessage = (message) => {
    console.log(message)
        const createIOUMsgModal = $uibModal.open({
            templateUrl: 'createIOUMsgModal.html',
            controller: 'createIOUMsgModalCtrl',
            controllerAs: 'createIOUMsgModal',
            resolve: {
                message: () => message
            }
        });

        // No behaviour on close / dismiss.
        createIOUMsgModal.result.then(() => {}, () => { location.reload();}, () => { location.reload();});
        // createIOUMsgModal.result.then(() => {}, () => { demoApp.refresh();}, () => { demoApp.refresh();});
    };

    /** Closes the IOU creation modal. */
    createIOUModal.cancel = () => $uibModalInstance.dismiss();

    // Validates the IOU.
    function validFormInput() {
        var ds= isNaN(createIOUModal.form.fundId)
        var s = createIOUModal.form.transactionType !== null
                var s1 = createIOUModal.form.amount !== null
     return isNaN(createIOUModal.form.fundId) || (createIOUModal.form.transactionType === undefined|| (createIOUModal.form.amount === undefined));
    }
});

// Controller for the success/fail modal.
angular.module('demoAppModule').controller('createIOUMsgModalCtrl', function($uibModalInstance, message) {
    const createIOUMsgModal = this;
    createIOUMsgModal.message = message.data;
});