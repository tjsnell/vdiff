angular.module('myApp', [], function ($locationProvider) {
   //enable html5 mode
   $locationProvider.html5Mode(true);
   console.log("INIT the location Provider")
});

function DiffCtrl($scope, $http, $routeParams, $location) {

   $scope.diff = function () {
      console.log("diff()");
      if (!$scope.v1 || !$scope.v2) {
         alert("Both versions must be set");
         return;
      }
      $("body").css("cursor", "wait");
      $http.get(location.pathname + "vdiff/compare/" + $scope.orgName + "/" + $scope.projectName + "/" + $scope.v1 + "/" + $scope.v2)
         .success(function (data, status, headers, config) {
            $scope.added = sortObject(data.added);
            $scope.dropped = sortObject(data.dropped);
            $scope.changed = data.changed;
            $scope.unchanged = sortObject(data.unChanged);

            $scope.addedCount = Object.keys(data.added).length;
            $scope.droppedCount = Object.keys(data.dropped).length
            $scope.unchangedCount = Object.keys(data.unChanged).length

            $("body").css("cursor", "default");
         })
         .error(function (data, status, headers, config) {
            console.log("Status: " + status);
            $("body").css("cursor", "default")
         });

   };

   $scope.init = function () {

      console.log('init()');

      var params = $location.hash().split('/');

      console.log('Params: ' + params);
      // set defaults
      $scope.orgName = 'apache';
      $scope.projectName = 'camel';

      switch (params.length) {
         case 4:
            $scope.orgName = params[0];
            $scope.projectName = params[1];
            $scope.v1 = params[2];
            $scope.v2 = params[3];
            break;
         case 2:
            $scope.orgName = params[0];
            $scope.projectName = params[1];
            break;
      }

      console.log('Org: ' + $scope.orgName + ' Project: ' + $scope.projectName);


      if ($scope.v1 != null && $scope.v2 != null) {
         $scope.diff();
      }


      $("body").css("cursor", "wait");
      $http.get(location.pathname + "vdiff/tags/" + $scope.orgName + "/" + $scope.projectName)
         .success(function (data, status, headers, config) {
            $scope.optionsList = angular.fromJson(data);
            $scope.optionsList1 = $scope.optionsList;
            $scope.optionsList2 = $scope.optionsList;
            $("body").css("cursor", "default");
         })
         .error(function (data, status, headers, config) {
            console.log("Status-init: " + status);
            $("body").css("cursor", "default");
         });
   };

   $scope.changed1 = function () {
      if ($scope.v1) {
         var rp = true;
         $scope.optionsList2 = $scope.optionsList.filter(function (item) {
            if (rp && $scope.v1 == item) {
               rp = false;
               return false;
            }
            return rp;
         });
      } else {
         $scope.optionsList2 = $scope.optionsList;
      }
   }

   $scope.changed2 = function () {
      if ($scope.v2) {
         var rp = false;
         $scope.optionsList1 = $scope.optionsList.filter(function (item) {
            if (!rp && $scope.v2 == item) {
               rp = true;
               return false;
            }
            return rp;
         });
      } else {
         $scope.optionsList1 = $scope.optionsList;
      }
   };

}


function sortObject(o) {
   var sorted = {},
      key, a = [];

   for (key in o) {
      if (o.hasOwnProperty(key)) {
         a.push(key);
      }
   }

   a.sort();

   for (key = 0; key < a.length; key++) {
      sorted[a[key]] = o[a[key]];
   }
   return sorted;
}
