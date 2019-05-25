/*
* 加密算法
* */
initEncyptArray.prototype = {

    'int': function (e) {
        return encryptInt(e, this.dict)
    },
    iary: function (mArray) {
        for (var t = '', n = 0; n < mArray.length; n++) {
            var r = encryptInt(mArray[n], this.dict2);
            t += r.length > 1 ? r.length - 2 + r : r
        }
        return t
    },
    bary: function (mArray) {
        for (var t = 0, n = {}, r = 0; r < mArray.length; r++)
            mArray[r] > t && (t = mArray[r],
                n[mArray[r]] = !0);
        var o = parseInt(t / 6);
        o += t % 6 ? 1 : 0;
        for (var a = '', r = 0; o > r; r++) {
            for (var i = 6 * r, d = 0, c = 0; 6 > c; c++)
                n[i] && (d += Math.pow(2, c)),
                    i++;
            a += this.dict[d]
        }
        return a
    },
    str: function (e) {
        for (var t = [], n = 0; n < e.length; n++) {
            var r = e.charCodeAt(n);
            r >= 1 && 127 >= r ? t.push(r) : r > 2047 ? (t.push(224 | r >> 12 & 15),
                t.push(128 | r >> 6 & 63),
                t.push(128 | r >> 0 & 63)) : (t.push(192 | r >> 6 & 31),
                t.push(128 | r >> 0 & 63))
        }
        for (var o = '', n = 0, a = t.length; a > n;) {
            var i = t[n++];
            if (n >= a) {
                o += this.dict[i >> 2],
                    o += this.dict[(3 & i) << 4],
                    o += '__';
                break
            }
            var d = t[n++];
            if (n >= a) {
                o += this.dict[i >> 2],
                    o += this.dict[(3 & i) << 4 | (240 & d) >> 4],
                    o += this.dict[(15 & d) << 2],
                    o += '_';
                break
            }
            var c = t[n++];
            o += this.dict[i >> 2],
                o += this.dict[(3 & i) << 4 | (240 & d) >> 4],
                o += this.dict[(15 & d) << 2 | (192 & c) >> 6],
                o += this.dict[63 & c]
        }
        return o
    }

}


/**
 * 初始化一个加密数组
 * @param token
 */
function initEncyptArray(token) {
    var encyptArray = [[48, 57], [65, 90], [97, 122], [45, 45], [126, 126]]
        , dict = expandArray(encyptArray)
        , dict2 = expandArray(encyptArray.slice(1));
    token && (dict = funReoder(dict, token),
        dict2 = funReoder(dict2, token)),
        this.dict = dict,
        this.dict2 = dict2
}

/**
 * 重排序encryptArray
 * @param encryptArray
 * @param token
 * @returns {*}
 */
function funReoder(encryptArray, token) {
    for (var tokenArray = token.split(''), r = 0; r < encryptArray.length; r++) {
        var encryptArrayIndex = r % tokenArray.length;
        encryptArrayIndex = tokenArray[encryptArrayIndex].charCodeAt(0),
            encryptArrayIndex %= encryptArray.length;
        var encryptArrayChar = encryptArray[r];
        encryptArray[r] = encryptArray[encryptArrayIndex],
            encryptArray[encryptArrayIndex] = encryptArrayChar
    }
    return encryptArray
}

/**
 * 展开一个二维数组
 * @param mExpandArray
 * @returns {Array}
 */
function expandArray(mExpandArray) {
    for (var arrayRel = [], n = 0; n < mExpandArray.length; n++)
        for (var r = mExpandArray[n][0]; r <= mExpandArray[n][1]; r++)
            arrayRel.push(String.fromCharCode(r));
    return arrayRel
}

/**
 * 转化某个int类型字符串
 * @param encyptIntStr
 * @param encryptDictitonArray
 * @returns {*}
 */
function encryptInt(encyptIntStr, encryptDictitonArray) {
    var rel = '';
    var encyptInt = Math.abs(parseInt(encyptIntStr));
    if (encyptInt) {
        for (; encyptInt;) {
            rel += encryptDictitonArray[encyptInt % encryptDictitonArray.length],
                encyptInt = parseInt(encyptInt / encryptDictitonArray.length);
        }
    } else {
        rel = encryptDictitonArray[0];
    }


    return rel
}


/**
 *  用加密算法拼接信息
 * @param info
 * @param token
 * @returns {string}
 */
function joinBaseBrowserMsg(info, token) {

    var encyptObj = new initEncyptArray(token);

    var broserInfo = {
        flashInfo: 0,
        mouseDown: 1,
        keyDown: 2,
        mouseMove: 3,
        version: 4,
        loadTime: 5,
        browserInfo: 6,
        token: 7,
        location: 8,
        screenInfo: 9
    };

    var resultArray = [encyptObj.iary([2])];


    for (var infoProperty in info) {

        var infoPropertyEntity = info[infoProperty];

        if (void 0 !== infoPropertyEntity && void 0 !== broserInfo[infoProperty]) {
            var temp;

            if ('number' == typeof infoPropertyEntity) {
                (temp = infoPropertyEntity >= 0 ? 1 : 2, infoPropertyEntity = encyptObj.int(infoPropertyEntity));
            } else {
                if ('boolean' == typeof infoPropertyEntity) {
                    (temp = 3, infoPropertyEntity = encyptObj.int(infoPropertyEntity ? 1 : 0))
                } else {
                    if ('object' == typeof infoPropertyEntity && infoPropertyEntity instanceof Array) {
                        (temp = 4, infoPropertyEntity = encyptObj.bary(infoPropertyEntity))
                    } else {
                        (temp = 0, infoPropertyEntity = encyptObj.str(infoPropertyEntity + ''))
                    }
                }
            }

            infoPropertyEntity && resultArray.push(encyptObj.iary([broserInfo[infoProperty], temp, infoPropertyEntity.length]) + infoPropertyEntity);

        }
    }
    return resultArray.join('')
}







var token = 'tk' + Math.random() + (new Date).getTime()



var info = {
    "mouseDown": "",
    "keyDown": "",
    "mouseMove": "",
    "version": 26,
    "loadTime": (new Date()).valueOf(),
    "browserInfo": "3,2,74",
    "token": "tk0.149654001572015361558590996067",
    "location": ",undefined",
    "screenInfo": "23,0,1904,44,1920,1080,1920,1920,992"
}



var result = token + '@' + joinBaseBrowserMsg(info, token)

result