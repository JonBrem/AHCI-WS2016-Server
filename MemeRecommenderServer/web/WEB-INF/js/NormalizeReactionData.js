
function fixScales(fileContents) {
    for(var i = 0; i < fileContents.length; i++) {
        for(var j = 0; j < scalesToFix.length; j++) {
            if(fileContents[i][scalesToFix[j] + "X"] != "NA") {
                fileContents[i][scalesToFix[j] + "X"] = fixScaledX(fileContents[i][scalesToFix[j] + "X"]);
                fileContents[i][scalesToFix[j] + "Y"] = fixScaledY(fileContents[i][scalesToFix[j] + "Y"]);
            }
        }

        // fileContents[i]["faceWidth"] = fileContents[i]["faceWidth"] / 2.25;
        // fileContents[i]["faceHeight"] = fileContents[i]["faceHeight"] / 2.25;
    }
    return fileContents;
}

function fixScaledX(x) {
    return (x - 1080) * (-1) / 2.25;
}

function fixScaledY(y) {
    return y / 2.25;
}


function normalizeData(reactionData) {
    normalize = true;
    normalizedFileContents = [];
    for(var i = 0; i < reactionData.length; i++) {
        var faceDate = getCopy(reactionData[i]);
        fixEulerZRotation(faceDate);
        fixPosition(faceDate);
        fixSize(faceDate);

        normalizedFileContents.push(faceDate);
    }    
}

function fixPosition(faceData) {
    var canvasCenterX = canvas.width / 2;
    var canvasCenterY = canvas.height / 2;

    var moveByX = (faceData.faceX + faceData.faceWidth / 2) - canvasCenterX;
    var moveByY = (faceData.faceY + faceData.faceHeight / 2) - canvasCenterY;

    for(var i = 0; i < positionKeys.length; i++) {
        if(faceData[positionKeys[i] + "X"] != "NA") {
            faceData[positionKeys[i] + "X"] -= moveByX;
            faceData[positionKeys[i] + "Y"] -= moveByY;
        }
    }
}

function fixSize(faceData) {
    
}

function getCopy(faceData) {
    var other = {};

    for(var key in faceData) {
        other[key] = faceData[key];
    }

    return other;
}

function fixEulerZRotation(faceData) {
    var faceX = faceData.faceX;
    var faceY = faceData.faceY;
    var angle = faceData.eulerZ;

    for(var i = 0; i < fixRotations.length; i++) {
        if(faceData[fixRotations[i] + "X"] != "NA") {

            var newPoint = rotateAround(faceData[fixRotations[i] + "X"],
                                        faceData[fixRotations[i] + "Y"],
                                        faceX, faceY, -1 * angle);
            faceData[fixRotations[i] + "X"] = newPoint.x;
            faceData[fixRotations[i] + "Y"] = newPoint.y;
        }
    }
}

// rotates point A in a circle with point B at its center by the angle
function rotateAround(pointAX, pointAY, pointBX, pointBY, angle) {
    var existingAngle = getAngle(pointAX, pointAY, pointBX, pointBY);
    var newAngle = existingAngle - angle + 90;

    var hypothenuseLength = Math.sqrt((pointAX - pointBX) * (pointAX - pointBX) + (pointAY - pointBY) * (pointAY - pointBY));

    var newAX = pointBX + Math.sin(newAngle * Math.PI / 180) * hypothenuseLength;
    var newAY = pointBY + Math.cos(newAngle * Math.PI / 180) * hypothenuseLength;

    return {"x" : newAX, "y" : newAY};
}

// the angle at point B if you create a triangle by going horizontally from point B X to point A X and then vertically from point B Y to A Y
// ! works for a "correct" coordinate system where top = y increases, right = x increases
function getAngle(pointAX, pointAY, pointBX, pointBY) {
    var xDiff = pointAX - pointBX;
    var yDiff = pointBY - pointAY;

    var alpha = Math.atan2(yDiff, xDiff) * 180 / Math.PI;

    return alpha;
}