--是否开启url权限校验功能
if ngx.var.enableValidateSwitch == "false" then
    ngx.exit(ngx.OK)
end

local url = ngx.var.request_uri
local action = string.match(url, "^/[^%/]+/file/(%w+)/.+$")
--只有获取文件和下载文件需要校验权限
if action ~= "getFile" and action ~= "downloadFile" then
    ngx.exit(ngx.OK)
end

--当前请求的域名或者ip
local host = ngx.var.host
--文档服务器的域名或者ip(可以包含端口)
local serverHost = ngx.var.serverHost
--当当前请求的域名(或者ip)和文档服务器的域名(或者ip)不相同时，不通过
if serverHost ~= host then
    ngx.exit(ngx.HTTP_FORBIDDEN)
end

local signLevel = string.match(url, "^/[^%/]+/file/%w+/(%w+)/.+$")
--文档服务器的校验级别
local serverSignLevel = ngx.var.signLevel
--url中的权限校验级别和当前文档服务器的校验级别不一致，校验不通过
if signLevel ~= serverSignLevel then
    ngx.exit(ngx.HTTP_FORBIDDEN)
end

--当文档服务器的校验级别为nn时表示不用校验
if signLevel == "nn" then
    ngx.exit(ngx.OK)
end

--公司编号
local corpCode = string.match(url, "^/[^%/]+/file/%w+/%w+/[^%/]+/([%w._-]+)/.+$")
--没有corpCode不能通过权限校验
if corpCode == "" or corpCode == nil then
    ngx.exit(ngx.HTTP_FORBIDDEN)
end

--用于切分字符串
function string:split(sep)
    local sep, fields = sep or "\t", {}
    local pattern = string.format("([^%s]+)", sep)
    self:gsub(pattern, function(c) fields[#fields + 1] = c end)
    return fields
end

--不校验权限的公司列表
local excludeCorpCodes = ngx.var.excludeCorpCodes;
if excludeCorpCodes ~= "" and excludeCorpCodes ~= nil then
    local excludeCorpCodeList = string.split(excludeCorpCodes, ",")
    for k, v in pairs(excludeCorpCodeList)
    do
        if corpCode == v then
            ngx.exit(ngx.OK)
        end
    end
end

--应用编号
local appCode = string.match(url, "^/[^%/]+/file/%w+/%w+/[^%/]+/[%w._-]+/(%w+)/.+$")
--没有appCode不能通过权限校验
if appCode == "" or appCode == nil then
    ngx.exit(ngx.HTTP_FORBIDDEN)
end

local fileType = string.match(url, "^/[^%/]+/file/%w+/%w+/[^%/]+/[%w._-]+/%w+/(%w+)/.+$")
--gen表示生成文件，src表示源文件，不是生成文件又不是源文件的请求不能通过验权
if fileType ~= "gen" and fileType ~= "src" then
    ngx.exit(ngx.HTTP_FORBIDDEN)
end

--文件在文件系统中的主键
local fileId;
if fileType == "gen" then
    fileId = string.match(url, "^/[^%/]+/file/%w+/%w+/[^%/]+/[%w._-]+/%w+/gen/%w+/%d+/(%w+)/.+$")
else
    fileId = string.match(url, "^/[^%/]+/file/%w+/%w+/[^%/]+/[%w._-]+/%w+/src/.+/%d+/%w+/(%w+)%.%w+$")
end

--文件在文件系统中的主键不存在不能通过验权
if fileId == "" or fileId == nil then
    ngx.exit(ngx.HTTP_FORBIDDEN)
end

--不校验权限的资源ID列表
local excludeResources = ngx.var.excludeResources
if excludeResources ~= "" and excludeResources ~= nil then
    local excludeResourceList = string.split(excludeResourceList, ",")
    for k, v in pairs(excludeResourceList)
    do
        if fileId == v then
            ngx.exit(ngx.OK)
        end
    end
end

local signFlag = true
local excludeSecretCorpCodes = ngx.var.excludeSecretCorpCodes
if excludeSecretCorpCodes ~= "" and excludeSecretCorpCodes ~= nil then
    local excludeSecretCorpCodeList = string.split(excludeSecretCorpCodes, ",")
    for k, v in pairs(excludeSecretCorpCodeList)
    do
        if corpCode == v then
            signFlag = false
            break
        end
    end
end

local timeFlag = true
local excludeTimeCorpCodes = ngx.var.excludeTimeCorpCodes
if excludeTimeCorpCodes ~= "" and excludeTimeCorpCodes ~= nil then
    local excludeTimeCorpCodeList = string.split(excludeTimeCorpCodes, ",")
    for k, v in pairs(excludeTimeCorpCodeList)
    do
        if corpCode == v then
            timeFlag = false
            break
        end
    end
end

local sessionFlag = true
local excludeSessionCorpCodes = ngx.var.excludeSessionCorpCodes
if excludeSessionCorpCodes ~= "" and excludeSessionCorpCodes ~= nil then
    local excludeSessionCorpCodeList = string.split(excludeSessionCorpCodes, ",")
    for k, v in pairs(excludeSessionCorpCodeList)
    do
        if corpCode == v then
            sessionFlag = false
            break
        end
    end
end

--用户登录session
local sid
--文档url校验文档服务器的秘钥 SECRET
if signLevel == "st" then
    --当前公司不需要验证签名（秘钥），直接通过
    if signFlag == false then
        ngx.exit(ngx.OK)
    end

    local sign = string.match(url, "^/[^%/]+/file/%w+/%w+/(%w+)/.+$")
    --当前url的签名为空，验证不通过
    if sign == "" or sign == nil then
        ngx.exit(ngx.HTTP_FORBIDDEN)
    end

    --文档服务器的秘钥
    local secret = ngx.var.secret
    local signText = secret .. "|" .. serverHost .. "|" .. signLevel .. "|" .. corpCode
            .. "|" .. appCode .. "|" .. fileId .. "|" .. secret;
    local genSign = ngx.md5(signText);
    if sign == genSign then
        ngx.exit(ngx.OK)
    else
        ngx.exit(ngx.HTTP_FORBIDDEN)
    end
    --文档url校验过期时间和文档服务器的秘钥
elseif signLevel == "stt" then
    --当前公司不需要验证时间和签名（秘钥），直接通过
    if timeFlag == false and signFlag == false then
        ngx.exit(ngx.OK)
    end

    local sign = string.match(url, "^/[^%/]+/file/%w+/%w+/(%w+)_%d+/.+$")
    local timestamp = string.match(url, "^/[^%/]+/file/%w+/%w+/%w+_(%d+)/.+$")
    --时间戳不存在或者签名不存在，验证不通过
    if sign == "" or sign == nil or timestamp == "" or timestamp == nil or timestamp == 0 then
        ngx.exit(ngx.HTTP_FORBIDDEN)
    end

    if timeFlag == true then
        local nowTime = ngx.now() * 1000
        local urlExpireTime = ngx.var.urlExpireTime * 60 * 1000
        --url已经过期，验证不通过
        if (nowTime - timestamp) > urlExpireTime then
            ngx.exit(ngx.HTTP_FORBIDDEN)
        end
    end

    if signFlag == true then
        --文档服务器的秘钥
        local secret = ngx.var.secret
        local signText = secret .. "|" .. timestamp .. "|" .. serverHost .. "|" .. signLevel ..
                "|" .. corpCode .. "|" .. appCode .. "|" .. fileId .. "|" .. secret;
        local genSign = ngx.md5(signText);
        --签名不正确，验证不通过
        if sign ~= genSign then
            ngx.exit(ngx.HTTP_FORBIDDEN)
        end
    end

    --时间戳和签名验证通过
    ngx.exit(ngx.OK)
    --文档url校验文档服务器的登录session
elseif signLevel == "sn" then
    --当前公司不需要验证登录session，直接通过
    if sessionFlag == false then
        ngx.exit(ngx.OK)
    end

    sid = string.match(url, "^/[^%/]+/file/%w+/%w+/([^%/]+)/.+$")
    --登录session为空，验证不通过
    if sid == "" or sid == nil then
        ngx.exit(ngx.HTTP_FORBIDDEN)
    end
    --文档url校验过期时间，文档服务器的秘钥和登录session
elseif signLevel == "sts" then
    --当前公司不需要验证签名，时间和登录session，直接通过
    if timeFlag == false and signFlag == false and sessionFlag == false then
        ngx.exit(ngx.OK)
    end

    local sign = string.match(url, "^/[^%/]+/file/%w+/%w+/(%w+)_%d+_[^%/]+/.+$")
    local timestamp = string.match(url, "^/[^%/]+/file/%w+/%w+/%w+_(%d+)_[^%/]+/.+$")
    sid = string.match(url, "^/[^%/]+/file/%w+/%w+/%w+_%d+_([^%/]+)/.+$")
    --时间戳不存在或者签名不存在或者session不存在，验证不通过
    if sign == "" or sign == nil or timestamp == ""
            or timestamp == nil or timestamp == 0
            or sid == "" or sid == nil then
        ngx.exit(ngx.HTTP_FORBIDDEN)
    end

    if timeFlag == true then
        local nowTime = ngx.now() * 1000
        local urlExpireTime = ngx.var.urlExpireTime * 60 * 1000
        --url已经过期，验证不通过
        if (nowTime - timestamp) > urlExpireTime then
            ngx.exit(ngx.HTTP_FORBIDDEN)
        end
    end

    if signFlag == true then
        --文档服务器的秘钥
        local secret = ngx.var.secret
        local signText = secret .. "|" .. timestamp .. "|" .. sid .. "|" .. serverHost ..
                "|" .. signLevel .. "|" .. corpCode .. "|" .. appCode .. "|" .. fileId .. "|" .. secret;
        local genSign = ngx.md5(signText);
        --签名不正确，验证不通过
        if sign ~= genSign then
            ngx.exit(ngx.HTTP_FORBIDDEN)
        end
    end
else
    --不认识的校验级别，验证不通过
    ngx.exit(ngx.HTTP_FORBIDDEN)
end

if sessionFlag == false then
    ngx.exit(ngx.OK)
end

--共享内存
local store = require'store'
--当前时间毫秒数
local nowTime = ngx.now() * 1000
--session验证结果缓存时间（单位为秒）
local sessionValidCacheTime = ngx.var.sessionValidCacheTime
--校验url，每个环境的host都不一样
--local checkurl = ngx.var.checkSidUrl
local checkurl
if appCode == 'live' then
    checkurl = 'http://' .. ngx.var.remote_addr .. ':3013/checkSid'
else
    checkurl = ngx.var.checkSidUrl
end
--验证session的签名秘钥，默认为sf。
local sessionSignSecret = ngx.var.sessionSignSecret
local check = store.get(sid)
if (check == nil) then
    local http = require"http"
    local httpc = http.new()
    httpc:set_timeout(3000)
    local sign = ngx.md5(sid .. '|' .. sessionSignSecret)
    local res, err = httpc:request_uri(checkurl, {
        method = "POST",
        body = "sid=" .. sid .. "&sign=" .. sign,
        headers = {
            ["Content-Type"] = "application/x-www-form-urlencoded",
        }
    })

    if not res then
        --由uc，网络等造成的请求失败，不保存缓存
        ngx.exit(ngx.HTTP_FORBIDDEN)
    elseif res.body == 't' then
        store.set(sid, nowTime, sessionValidCacheTime)
        ngx.exit(ngx.OK)
    else
        store.set(sid, -nowTime, sessionValidCacheTime)
        ngx.exit(ngx.HTTP_FORBIDDEN)
    end
elseif check < 0 and nowTime + check < sessionValidCacheTime * 1000 then
    ngx.exit(ngx.HTTP_FORBIDDEN)
elseif check < 0 or nowTime - check >= sessionValidCacheTime * 1000 then
    store.remove(sid)
    local http = require"http"
    local httpc = http.new()
    httpc:set_timeout(3000)
    local sign = ngx.md5(sid .. '|' .. sessionSignSecret)
    local res, err = httpc:request_uri(checkurl, {
        method = "POST",
        body = "sid=" .. sid .. "&sign=" .. sign,
        headers = {
            ["Content-Type"] = "application/x-www-form-urlencoded",
        }
    })

    if not res then
        --由uc，网络等造成的请求失败，不保存缓存
        ngx.exit(ngx.HTTP_FORBIDDEN)
    elseif res.body == 't' then
        store.set(sid, nowTime, sessionValidCacheTime)
        ngx.exit(ngx.OK)
    else
        store.set(sid, -nowTime, sessionValidCacheTime)
        ngx.exit(ngx.HTTP_FORBIDDEN)
    end
else
    ngx.exit(ngx.OK)
end

--nginx中配置的参数
--enableValidateSwitch 是否开启文件校验
--serverHost 文档服务器的域名或者ip(可以包含端口)
--signLevel 文档服务器的校验级别
--excludeCorpCodes 所有权限都不校验权限的公司列表
--excludeResources 不校验权限的资源ID列表
--secret 文档服务器的秘钥
--excludeSecretCorpCodes 不校验秘钥权限的公司列表
--excludeTimeCorpCodes 不校验时间权限的公司列表
--excludeSessionCorpCodes 不校验session权限的公司列表
--urlExpireTime url的实效时间（单位为分钟），默认1440(24*60)。
--checkSidUrl 用于检验session是否正确的url
--sessionValidCacheTime session验证结果缓存时间（单位为秒），默认180。
--sessionSignSecret 验证session的签名秘钥，默认为sf。








