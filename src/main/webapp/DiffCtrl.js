angular.module('myApp', ['ngCookies'], function ($locationProvider) {
   //enable html5 mode
   $locationProvider.html5Mode(true);
   console.log("INIT the location Provider.")
});

function DiffCtrl($scope, $http, $routeParams, $location, $cookies) {

   $scope.diff = function () {
      console.log("diff()");
      if (!$scope.ver1 || !$scope.ver2) {
         alert("Both versions must be set");
         return;
      }
      $("body").css("cursor", "wait");
      $location.path("diff/" + $scope.orgName+ "/" + $scope.projectName + "/" + $scope.ver1 + "/" + $scope.ver2);
      $http.get("/vdiff/compare/" + $scope.orgName + "/" + $scope.projectName + "/" + $scope.ver1 + "/" + $scope.ver2)
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

      $scope.showAdded = false;

      if (typeof $cookies.org === 'undefined' ) {
         // set defaults
         $scope.orgName = 'apache';
         $scope.projectName = 'camel';
      } else {
         $scope.orgName = $cookies.org;
         $scope.projectName = $cookies.proj;
      }
      $location.path("diff/" + $scope.orgName + "/" + $scope.projectName);

      if (typeof $cookies.ver1 !== 'undefined') {
         $scope.ver1 = $cookies.ver1;
         $scope.ver2 = $cookies.ver2;
      } else {
         console.log('no cookie for me');
      }

      console.log('Org: ' + $scope.orgName + ' Project: ' + $scope.projectName);
      console.log('ver1: ' + $scope.ver1 + ' ver2: ' + $scope.ver2);

      if ($scope.ver1 != null && $scope.ver2 != null) {
         $scope.diff();
      }


      $("body").css("cursor", "wait");
      /* TODO unfortunately adding in the path breaks of you use the filename. */
      $http.get("/vdiff/tags/" + $scope.orgName + "/" + $scope.projectName)
         .success(function (data, status, headers, config) {
            $scope.optionsList = angular.fromJson(data);
            $scope.optionsList1 = $scope.optionsList;
            $scope.optionsList2 = $scope.optionsList;
            $scope.optionsList2.unshift('trunk'); // only new version needs trunk
            $("body").css("cursor", "default");
            console.log($scope.optionsList);
         })
         .error(function (data, status, headers, config) {
            console.log("Status-init: " + status);
            $("body").css("cursor", "default");
         });
   };

   $scope.changed1 = function () {
      if ($scope.ver1) {
         var rp = true;
         $scope.optionsList2 = $scope.optionsList.filter(function (item) {
            if (rp && $scope.ver1 == item) {
               rp = false;
               return false;
            }
            return rp;
         });
      } else {
         $scope.optionsList2 = $scope.optionsList;
      }
   };

   $scope.toggleAdded = function() {
      $scope.showAdded = !$scope.showAdded;
   }

   $scope.toggleDropped = function() {
      $scope.showDropped = !$scope.showDropped;
   }

   $scope.toggleChanged = function() {
      $scope.showChanged = !$scope.showChanged;
   }

   $scope.toggleUnchanged = function() {
      $scope.showUnchanged = !$scope.showUnchanged;
   }



   $scope.changed2 = function () {
      if ($scope.ver2) {
         var rp = false;
         $scope.optionsList1 = $scope.optionsList.filter(function (item) {
            if (!rp && $scope.ver2 == item) {
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
