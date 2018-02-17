"use strict";

// Define your backend here.
angular.module('demoAppModule', ['ui.bootstrap']).controller('DemoAppCtrl', function($http, $location, $uibModal) {
    const demoApp = this;

    //const apiBaseURL = "http://localhost:10019/api/iou/";
    const apiBaseURL = "/api/iou/";


    // Retrieves the identity of this and other nodes.
    let peers = [];
    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);
    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);


    /** Displays the IOU creation modal. */
    demoApp.openCreateIOUModal = () => {
        const createIOUModal = $uibModal.open({
            templateUrl: 'createIOUModal.html',
            controller: 'CreateIOUModalCtrl',
            controllerAs: 'createIOUModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        // Ignores the modal result events.
        createIOUModal.result.then(() => { }, () => { });
    };

    /** Displays the cash issuance modal. */
    demoApp.openIssueCashModal = (id) => {
        const issueCashModal = $uibModal.open({
            templateUrl: 'issueCashModal.html',
            controller: 'IssueCashModalCtrl',
            controllerAs: 'issueCashModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                id: () => id
            }
        });

        issueCashModal.result.then(() => {}, () => { });
    };

    /** Displays the IOU transfer modal. */
    demoApp.openTransferModal = (id) => {
        const transferModal = $uibModal.open({
            templateUrl: 'transferModal.html',
            controller: 'TransferModalCtrl',
            controllerAs: 'transferModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => peers,
                id: () => id
            }
        });

        transferModal.result.then(() => { }, () => { });
    };

   /** Displays the IOU transfer modal. */
    demoApp.openNavModal = (id) => {
        const navModal = $uibModal.open({
            templateUrl: 'navModal.html',
            controller: 'navModalCtrl',
            controllerAs: 'navModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => peers,
                id: () => id
            }
        });

        navModal.result.then(() => { }, () => { });
    };
    /** Displays the IOU settlement modal. */
    demoApp.openSettleModal = (id) => {
        const settleModal = $uibModal.open({
            templateUrl: 'settleModal.html',
            controller: 'SettleModalCtrl',
            controllerAs: 'settleModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                id: () => id
            }
        });

        settleModal.result.then(() => {}, () => {});
    };

    //To intitate the sell javascript

        demoApp.opensellModal = () => {
            const sellModal = $uibModal.open({
                templateUrl: 'sellModal.html',
                controller: 'sellModalCtrl',
                controllerAs: 'sellModal',
                resolve: {
                    apiBaseURL: () => apiBaseURL
                }
            });

            sellModal.result.then(() => { }, () => { });
        };

        demoApp.AandVModal = () => {
                    const AandV = $uibModal.open({
                        templateUrl: 'AandVModal.html',
                        controller: 'AandVCtrl',
                        controllerAs: 'AandVModal',
                        resolve: {
                            apiBaseURL: () => apiBaseURL
                        }
                    });

                    AandV.result.then(() => { }, () => { });
                };

    /** Refreshes the front-end. */
    demoApp.refresh = () => {
        // Update the list of IOUs.
        $http.get(apiBaseURL + "ious").then((response) => demoApp.ious =
            Object.keys(response.data).map((key) => response.data[key].state.data));
        $http.get(apiBaseURL + "total_holding").then((response) => demoApp.holding = response.data);

        $http.get(apiBaseURL + "register_book").then((response) => demoApp.register = response.data);
/*
        Need to remove this after this change*/
        $http.get(apiBaseURL + "nav_json").then((response) => demoApp.nav = response.data);

         $http.get(apiBaseURL + "navvalues").then((response) => demoApp.navValues = response.data[response.data.length-1].state.data.nav);
//nav
    }

    demoApp.refresh();
});

// Causes the webapp to ignore unhandled modal dismissals.
angular.module('demoAppModule').config(['$qProvider', function($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);
