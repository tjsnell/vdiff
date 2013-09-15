function DiffCtrl($scope, $http) {

    $scope.diff = function () {
//        $scope.$evalAsync(
//            $("body").css("cursor", "wait")
//        );
        $http.get("/vdiff/" + $scope.v1 + "/" + $scope.v2)
            .success(function (data, status, headers, config) {
                $scope.added = sortObject(data.added);
                $scope.dropped = sortObject(data.dropped);
                $scope.changed = data.changed;
                $scope.unchanged = sortObject(data.unChanged);
//                $scope.$evalAsync(
//                    $("body").css("cursor", "default")
//                )
            })
            .error(function (data, status, headers, config) {
                console.log("Status: " + status);
//                $scope.$evalAsync(
//                    $("body").css("cursor", "default")
//                );
            });

    };

    $scope.init = function () {
//        $scope.$evalAsync(
            $("body").css("cursor", "wait")
//        );
        $http.get("/vdiff/tags")
            .success(function (data, status, headers, config) {
                $scope.optionsList = angular.fromJson(data);
//                $scope.$evalAsync(
//                    $("body").css("cursor", "default")
//                )
            })
            .error(function (data, status, headers, config) {
                console.log("Status-init: " + status);
//                $scope.$evalAsync(
//                    $("body").css("cursor", "default")
//                )
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

