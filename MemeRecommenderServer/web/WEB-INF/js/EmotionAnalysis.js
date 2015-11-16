jQuery(document).ready(function($) {
    init();
});

var canvas, ctx, keys = ["user","img","smiling","leftEyeX","leftEyeY","leftEyeOpen","rightEyeX","rightEyeY","rightEyeOpen","leftMouthX","leftMouthY","rightMouthX","rightMouthY","leftCheekX","leftCheekY","rightCheekX","rightCheekY","noseBaseX","noseBaseY","bottomMouthX","bottomMouthY","faceX","faceY","faceId","eulerY","eulerZ","faceWidth","faceHeight","selectedEmotion","timeStamp"],
    positionKeys = ["leftEye","rightEye","leftMouth","rightMouth","leftCheek","rightCheek","noseBase","bottomMouth","face"],
    emotionCodes = ["Neutral / Other", "Smile", "Grin", "Laughter", "ROFL"];

var fixRotations = ["leftEye", "rightEye", "leftMouth", "rightMouth", "bottomMouth", "noseBase", "leftEye", "rightEye", "leftCheek", "rightCheek"];
var scalesToFix = ["leftMouth", "rightMouth", "bottomMouth", "noseBase", "leftEye", "rightEye", "leftCheek", "rightCheek"];

var PLAYBACK_SPEED = 100;
var interval;

var loadFileButton;
var fileContents, normalizedFileContents, normalize = false;

var memeNumbers;

var currentlySelectedMemeData;

var currentMemeDataIndex;

var $prevFrameButton,
    $nextFrameButton,
    $playButton,
    $pauseButton,
    $backToStartButton,
    $normalizeButton;

function init() {

    loadFileButton = $("#load_file_button");
    loadFileButton.on("click", function(e) {
        pause();
        var fileName = $("#input_filename").val();
        $.ajax({
           "url" : "/analyze_meme_reaction/load_file",
            "data" : {
                "filename" : fileName
            },
            "success" : loadMeme,
            "error" : function(e) {console.log(e);}
        });
    });

    setupButtons();
}

function setupButtons() {
    $prevFrameButton = $("#play_controls_vid_back");
    $nextFrameButton = $("#play_controls_vid_next");
    $playButton = $("#play_controls_vid_play");
    $pauseButton = $("#play_controls_vid_pause");
    $backToStartButton = $("#play_controls_vid_restart");

    $prevFrameButton.on("click", function() {
        pause();
        loadPrevFrame();
    });

    $nextFrameButton.on("click", function() {
        pause();
        loadNextFrame();
    });

    $playButton.on("click", function() {
        pause();
        play();
    });

    $pauseButton.on("click", function() {pause();});

    $backToStartButton.on("click", function() {reStart();});

    $normalizeButton = $("#normalize_button");
    $normalizeButton.on("click", function() {normalizeData(fileContents)});
}

function loadMeme(e) {
    fileContents = fixScales(JSON.parse(e));
    setupCanvas();

    setupMemeButtons();

    if(memeNumbers.length > 0) {
        showDataFor(memeNumbers[0]);
    }
}


function showDataFor(index) {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    currentlySelectedMemeData = [];

    var faceDataArray = (normalize)? normalizedFileContents : fileContents;

    for(var i = 0; i < faceDataArray.length; i++) {
        if(faceDataArray[i].img == index) {
            currentlySelectedMemeData.push(faceDataArray[i]);
        }
    }

    currentMemeDataIndex = 0;

    $(".hidden").removeClass('hidden');

    showMemeData(currentlySelectedMemeData);

    showFrameData(currentMemeDataIndex);
    showFace(currentMemeDataIndex);
}


function setupMemeButtons() {
    $("#select_reaction_to_memes").empty();
    memeNumbers = [];

    var memeNum = -1;

    for(var i = 0; i < fileContents.length; i++) {
        if(fileContents[i].img != memeNum) {
            memeNumbers.push(fileContents[i].img);
            memeNum = fileContents[i].img;
        }
    }

    for(var i = 0; i < memeNumbers.length; i++) {
        $("#select_reaction_to_memes").append('<button type="button" class="btn btn-default show_data_for_meme" data-meme-num="' +
            memeNumbers[i] + '">' + 
            memeNumbers[i] + '</button>');
    }

    setTimeout(function() {
        $(".show_data_for_meme").on("click", function(e) {
            console.log("yay", e);
            showDataFor($(e.target).attr("data-meme-num"));
        })
    }, 100);

}

function loadNextFrame() {
    if(currentMemeDataIndex < currentlySelectedMemeData.length - 1) {
        currentMemeDataIndex++;
        clearCanvas();
        showFace(currentMemeDataIndex);
        showFrameData(currentMemeDataIndex);
    }
}

function play() {
    interval = setInterval(function() {
        loadNextFrame();
    }, PLAYBACK_SPEED);
}

function pause() {
    if(interval != undefined) {
        clearInterval(interval);
    }
}

function reStart() {
    currentMemeDataIndex = 0;
    clearCanvas();
    showFace(currentMemeDataIndex);
    showFrameData(currentMemeDataIndex);
}

function loadPrevFrame() {
    if(currentMemeDataIndex > 0) {
        currentMemeDataIndex--;
        clearCanvas();
        showFace(currentMemeDataIndex);
        showFrameData(currentMemeDataIndex);
    }
}

function clearCanvas() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
}

function showFace(index) {
    var faceData = currentlySelectedMemeData[index];
    drawFace(faceData, ctx);
}

function setupCanvas() {
    var minX = Number.MAX_VALUE, minY = Number.MAX_VALUE,
        maxX = Number.MIN_VALUE, maxY = Number.MIN_VALUE;

    for(var i = 0; i < fileContents.length; i++) {
        for(var pos = 0; pos < positionKeys.length; pos++) {
            if(fileContents[i][positionKeys[pos] + "X"] < minX) {
                minX = fileContents[i][positionKeys[pos] + "X"];
            }
            if(fileContents[i][positionKeys[pos] + "X"] > maxX) {
                maxX = fileContents[i][positionKeys[pos] + "X"];
                maxXIndex = i;
            }
            if(fileContents[i][positionKeys[pos] + "Y"] < minY) {
                minY = fileContents[i][positionKeys[pos] + "Y"];
            }
            if(fileContents[i][positionKeys[pos] + "Y"] > maxY) {
                maxY = fileContents[i][positionKeys[pos] + "Y"];
                maxYIndex = i;
            }
        }
    }

    createCanvas(Math.ceil(maxX), Math.ceil(maxY));
}

function createCanvas(width, height) {
    if($("#myCanvas").length > 0) {
        $("#myCanvas").remove();
    }

    $("#append_canvas").append('<canvas id="myCanvas" width="' + width + '" height="' + height + '" />');
    canvas = document.getElementById("myCanvas");
    ctx = canvas.getContext("2d");
}