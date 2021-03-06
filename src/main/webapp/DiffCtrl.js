angular.module('myApp', ['ngCookies'], function ($locationProvider) {
   //enable html5 mode
   $locationProvider.html5Mode(true);
   console.log("INIT the location Provider.")
});

function DiffCtrl($scope, $http, $routeParams, $location, $cookies, $window) {


   $scope.diff = function () {
      console.log("diff()");
//      if (!$scope.oldTag || !$scope.newTag) {
//         alert("Both versions must be set");
//         return;
//      }

      $("body").css("cursor", "wait");

      var older, newer;

      if ($scope.active_tab == 'tags') {
         $location.path("diff/"
            + $scope.orgName + "/"
            + $scope.projectName.toLowerCase()
            + "/tags/"
            + $scope.oldTag + "/"
            + $scope.newTag);

         older = $scope.tagOptions[$scope.oldTag];
         newer = $scope.tagOptions[$scope.newTag];
      } else {
         $location.path("diff/"
            + $scope.orgName + "/"
            + $scope.projectName.toLowerCase()
            + "/branches/"
            + $scope.oldBranch + "/"
            + $scope.newBranch);

         older = $scope.branchOptions[$scope.oldBranch];
         newer = $scope.branchOptions[$scope.newBranch];
      }

      $http.get("/vdiff/compare/" + $scope.orgName + "/" + $scope.projectName + "/" + older + "/" + newer)
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

   $scope.selectProject = function() {
      console.log("GOGO");
      $window.location.href = "/diff/"
         + $scope.project.org + "/" + $scope.project.name.toLowerCase();
   }

   $scope.shorten = function (str) {
      var reg = new RegExp($scope.projectName + "-(.*)");
      var shorter = str.match(reg);
      if (shorter) {
         return shorter[1];
      } else {
         return str;
      }
   };

   function setTagsFromCookies() {
      if ($scope.active_tab === 'tags') {
         if (typeof $cookies.oldTag !== 'undefined') {
            $scope.oldTag = $cookies.oldTag;
            $scope.newTag = $cookies.newTag;
            $scope.oldBranch = null;
            $scope.newBranch = null;
            $scope.diff();
         }
      }
   }

   function setBranchesFromCookies() {
      if ($scope.active_tab === 'branches') {
         if (typeof $cookies.oldTag !== 'undefined') {
            // todo make old new generic cookie names
            $scope.oldBranch = $cookies.oldTag;
            $scope.newBranch = $cookies.newTag;
            $scope.oldTag = null;
            $scope.newTag = null;
            $scope.diff();
         }
      }
   }

   function loadTags() {
      /* TODO unfortunately adding in the path breaks if index.html is in filename. */
      $http.get("/vdiff/tags/" + $scope.orgName + "/" + $scope.projectName)
         .success(function (data, status, headers, config) {
            $scope.tagsList = angular.fromJson(data);

            $scope.tagOptions = {};
            $scope.tagOptions['master'] = 'master';

            for (var i = 0; i < $scope.tagsList.length; i++) {
               shortName = $scope.shorten($scope.tagsList[i]);
               $scope.tagOptions[shortName] = $scope.tagsList[i];
               $scope.tagsList[i] = shortName;
            }

            $scope.oldTagsList = $scope.tagsList;
            $scope.newTagsList = $scope.tagsList.slice(0);
            $("body").css("cursor", "default");
            setTagsFromCookies();
         })
         .error(function (data, status, headers, config) {
            console.log("Status-init: " + status);
            $("body").css("cursor", "default");
         });
   }

   function loadBranches() {
      console.log('loading branches');
      $http.get("/vdiff/branches/" + $scope.orgName + "/" + $scope.projectName)
         .success(function (data, status, headers, config) {
            $scope.branchesList = angular.fromJson(data);

            $scope.branchOptions = {};

            for (var i = 0; i < $scope.branchesList.length; i++) {
               shortName = $scope.shorten($scope.branchesList[i]);
               $scope.branchOptions[shortName] = $scope.branchesList[i];
               $scope.branchesList[i] = shortName;
            }

            $scope.branchesList.reverse();

            $scope.oldBranchesList = $scope.branchesList;
            $scope.newBranchesList = $scope.branchesList.slice(0);
            $("body").css("cursor", "default");
            setBranchesFromCookies();
            console.log('brancheds loaded');
         })
         .error(function (data, status, headers, config) {
            console.log("Status-init: " + status);
            $("body").css("cursor", "default");
         });
   }

   $scope.init = function () {

      $("body").css("cursor", "wait");

      $scope.projects = {};
      $scope.projects['camel'] = { "name": "Camel", "org": "apache"};
      $scope.projects['karaf'] = { "name": "Karaf", "org": "apache"};
      $scope.projects['cxf'] = { "name": "CXF", "org": "apache"};
      $scope.projects['activemq'] = { "name": "ActiveMQ", "org": "apache"};
      $scope.projects['atmosphere'] = { "name": "Atmosphere", "org": "atmosphere"};


      if (typeof $cookies.activeTab !== 'undefined') {
         $scope.active_tab = $cookies.activeTab;
      } else {
         $scope.active_tab = 'tags';
      }


      if (typeof $cookies.org === 'undefined') {
         // set defaults
         $scope.orgName = 'apache';
         $scope.projectName = 'camel';
         $scope.project = $scope.projects["camel"];
      } else {
         $scope.orgName = $cookies.org;
         $scope.projectName = $cookies.proj;
         $scope.project = $scope.projects[$cookies.proj.toLowerCase()];
      }

      $location.path("diff/" + $scope.orgName + "/" + $scope.projectName);



      for (k in $scope.projects) {
         console.log('Project: ' + k);
      }
      loadTags();
      loadBranches();
   };


   $scope.oldTagUpdate = function () {
      if ($scope.oldTag) {
         var rp = true;
         $scope.newTagsList = $scope.tagsList.filter(function (item) {
            if (rp && $scope.oldTag == item) {
               rp = false;
               return false;
            }
            return rp;
         });
      } else {
         $scope.newTagsList = $scope.tagsList.slice(0);
      }
   };


   $scope.newTagUpdate = function () {
      if ($scope.newTag === 'master') {
         $scope.oldTagsList = $scope.tagsList;
      } else if ($scope.newTag) {
         var rp = false;
         $scope.oldTagsList = $scope.tagsList.filter(function (item) {
            if (!rp && $scope.newTag == item) {
               rp = true;
               return false;
            }
            return rp;
         });
      } else {
         $scope.oldTagsList = $scope.tagsList;
      }
   };

   $scope.oldBranchUpdate = function () {
      if ($scope.oldBranch && 1 == 2) {
         var rp = true;
         $scope.newBranchesList = $scope.branchesList.filter(function (item) {
            if (rp && $scope.oldBranch == item) {
               rp = false;
               return false;
            }
            return rp;
         });
      } else {
         $scope.newBranchesList = $scope.branchesList.slice(0);
      }
      $scope.newBranchesList.unshift('master'); // only new version needs master
   };


   $scope.newBranchUpdate = function () {
      if ($scope.newBranch && 1 == 2) {
         var rp = false;
         $scope.oldBranchesList = $scope.branchesList.filter(function (item) {
            if (!rp && $scope.newBranch == item) {
               rp = true;
               return false;
            }
            return rp;
         });
      } else {
         $scope.oldBranchesList = $scope.branchesList;
      }
   };

   $scope.capIt = function (string) {
      return string.charAt(0).toUpperCase() + string.slice(1);
   }

   $scope.toggleAdded = function () {
      $scope.showAdded = !$scope.showAdded;
   };

   $scope.toggleDropped = function () {
      $scope.showDropped = !$scope.showDropped;
   };

   $scope.toggleChanged = function () {
      $scope.showChanged = !$scope.showChanged;
   };

   $scope.toggleUnchanged = function () {
      $scope.showUnchanged = !$scope.showUnchanged;
   };

   $scope.init();

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


