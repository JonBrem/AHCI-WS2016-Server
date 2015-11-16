var EYE_SIZE = 5;
var EYE_COLOR = "#FF0000";

var MOUTH_SIZE = 5;
var MOUTH_CONNECTORS_WIDTH = 3;
var MOUTH_COLOR = "#333333";

var NOSE_SIZE = 5;
var NOSE_COLOR = "#00FF00";

var CHEEK_SIZE = 5;
var CHEEK_COLOR = "#0000FF";

function drawFace(faceData, ctx) {
    if(faceData == undefined) return;

    drawEyes(faceData, ctx);
    drawMouth(faceData, ctx);
    drawNose(faceData, ctx);
    drawCheeks(faceData, ctx);

    drawLinesBetweenCheeksAndMouth(faceData, ctx);
    drawLinesBetweenCheeksAndEyes(faceData, ctx);

    drawFaceFrame(faceData, ctx);
}

function drawFaceFrame(faceData, ctx) {
    ctx.strokeStyle = "#FF00FF";
    ctx.lineWidth = 2;

    var left = faceData.faceX;
    var top = faceData.faceY;
    
    ctx.strokeRect(left, top, faceData.faceWidth, faceData.faceHeight);
}

function drawLinesBetweenCheeksAndMouth(faceData, ctx) {
    if(faceData.leftCheekX == "NA" || faceData.rightCheekX == "NA") return;
    if(faceData.leftMouthX == "NA" || faceData.rightMouthX == "NA") return;

    ctx.strokeStyle = CHEEK_COLOR;
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(faceData.leftCheekX, faceData.leftCheekY);
    ctx.lineTo(faceData.leftMouthX, faceData.leftMouthY);
    ctx.stroke();
    ctx.closePath();
    ctx.beginPath();
    ctx.moveTo(faceData.rightCheekX, faceData.rightCheekY);
    ctx.lineTo(faceData.rightMouthX, faceData.rightMouthY);
    ctx.stroke();
    ctx.closePath();
}

function drawLinesBetweenCheeksAndEyes(faceData, ctx) {
    if(faceData.leftCheekX == "NA" || faceData.rightCheekX == "NA") return;
    if(faceData.leftEyeX == "NA" || faceData.rightEyeX == "NA") return;

    ctx.strokeStyle = EYE_COLOR;
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(faceData.leftCheekX, faceData.leftCheekY);
    ctx.lineTo(faceData.leftEyeX, faceData.leftEyeY);
    ctx.stroke();
    ctx.closePath();
    ctx.beginPath();
    ctx.moveTo(faceData.rightCheekX, faceData.rightCheekY);
    ctx.lineTo(faceData.rightEyeX, faceData.rightEyeY);
    ctx.stroke();
    ctx.closePath();
}

function drawCheeks(faceData, ctx) {
    if(faceData.leftCheekX == "NA" || faceData.rightCheekX == "NA") return;

    ctx.fillStyle = CHEEK_COLOR;
    ctx.beginPath();
    ctx.arc(faceData.leftCheekX,faceData.leftCheekY,CHEEK_SIZE,0,2*Math.PI);
    ctx.stroke();
    ctx.fill();
    ctx.beginPath();
    ctx.arc(faceData.rightCheekX,faceData.rightCheekY,CHEEK_SIZE,0,2*Math.PI);
    ctx.stroke();
    ctx.fill();
    ctx.closePath();
}

function drawNose(faceData, ctx) {
    if(faceData.noseBaseX == "NA") return;

    ctx.fillStyle = NOSE_COLOR;
    ctx.beginPath();
    ctx.arc(faceData.noseBaseX,faceData.noseBaseY,NOSE_SIZE,0,2*Math.PI);
    ctx.stroke();
    ctx.fill();
    ctx.closePath();
}

function drawMouth(faceData, ctx) {
    if(faceData.leftMouthX == "NA" || faceData.bottomMouthX == "NA" || faceData.rightMouthX == "NA") return;

    ctx.strokeStyle = MOUTH_COLOR;
    ctx.fillStyle = MOUTH_COLOR;
    ctx.lineWidth = MOUTH_CONNECTORS_WIDTH;
    ctx.beginPath();
    ctx.arc(faceData.leftMouthX,faceData.leftMouthY,EYE_SIZE,0,2*Math.PI);
    ctx.stroke();
    ctx.fill();
    ctx.closePath();
    ctx.beginPath();
    ctx.moveTo(faceData.leftMouthX,faceData.leftMouthY);
    ctx.arc(faceData.bottomMouthX,faceData.bottomMouthY,EYE_SIZE,0,2*Math.PI);
    ctx.stroke();
    ctx.fill();
    ctx.closePath();
    ctx.beginPath();
    ctx.moveTo(faceData.bottomMouthX,faceData.bottomMouthY);
    ctx.arc(faceData.rightMouthX,faceData.rightMouthY,EYE_SIZE,0,2*Math.PI);
    ctx.stroke();
    ctx.fill();
    ctx.closePath();

}

function drawEyes(faceData, ctx) {
    if(faceData.leftEyeX == "NA" || faceData.rightEyeX == "NA") return;

    ctx.fillStyle = EYE_COLOR;
    ctx.beginPath();
    ctx.arc(faceData.leftEyeX,faceData.leftEyeY,MOUTH_SIZE,0,2*Math.PI);
    ctx.stroke();
    ctx.fill();
    ctx.beginPath();
    ctx.arc(faceData.rightEyeX,faceData.rightEyeY,MOUTH_SIZE,0,2*Math.PI);
    ctx.stroke();
    ctx.fill();
    ctx.closePath();
}
