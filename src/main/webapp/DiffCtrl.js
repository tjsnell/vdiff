function DiffCtrl($scope, $http, $routeParams, $location) {

    $scope.diff = function () {
        $("body").css("cursor", "wait");
        $http.get("/vdiff/" + $scope.v1 + "/" + $scope.v2)
            .success(function (data, status, headers, config) {
                $scope.added = sortObject(data.added);
                $scope.dropped = sortObject(data.dropped);
                $scope.changed = data.changed;
                $scope.unchanged = sortObject(data.unChanged);

                $scope.addedCount = Object.keys(data.added).length;
                $scope.droppedCount = Object.keys(data.dropped).length
                $scope.unchangedCount =  Object.keys(data.unChanged).length

                console.log(data);
                console.log(Object.keys(data.added).length);

                $("body").css("cursor", "default");
            })
            .error(function (data, status, headers, config) {
                console.log("Status: " + status);
                $("body").css("cursor", "default")
            });

    };

    $scope.init = function ($location) {
        if ("v1" in $routeParams)
            console.log($routeParams['v1']);
        else
            console.log($location);
            console.log("V1 = " + $location.hash["v1"]);
        var vv1 = $location.search('v1');
        console.log(vv1);

        $("body").css("cursor", "wait");
        $http.get("/vdiff/tags")
            .success(function (data, status, headers, config) {
                $scope.optionsList = angular.fromJson(data);
                $("body").css("cursor", "default");
            })
            .error(function (data, status, headers, config) {
                console.log("Status-init: " + status);
                $("body").css("cursor", "default");
            });
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

