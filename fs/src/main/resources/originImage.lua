local imagePath = ngx.var.imagePath;
local ffmpeg = ngx.var.ffmpeg;
local originImage = string.match(imagePath, "^(.+/%w+%.%w+)_%d+_%d+_%d+_%d+%.%w+$")
if originImage ~= nil then
    local x = tonumber(string.match(imagePath, "^.+/%w+%.%w+_(%d+)_%d+_%d+_%d+%.%w+$"))
    local y = tonumber(string.match(imagePath, "^.+/%w+%.%w+_%d+_(%d+)_%d+_%d+%.%w+$"))
    local w = tonumber(string.match(imagePath, "^.+/%w+%.%w+_%d+_%d+_(%d+)_%d+%.%w+$"))
    local h = tonumber(string.match(imagePath, "^.+/%w+%.%w+_%d+_%d+_%d+_(%d+)%.%w+$"))
    local mr = io.popen(ffmpeg .. ' -i ' .. originImage .. ' 2>&1')
    local result = mr:read("*all")
    local width = tonumber(string.match(result, "Stream.*[^%d]+(%d+)x%d+[^%d]+"))
    local height = tonumber(string.match(result, "Stream.*[^%d]+%d+x(%d+)[^%d]+"))
    if (x + w) > width or (y + h) > height then
        --截图宽度超出图片的总宽度或者截图高度超出图片的总高度
        ngx.exit(ngx.HTTP_FORBIDDEN)
    end

    local exec = ffmpeg .. ' -i ' .. originImage .. ' -vf crop=' .. w .. ':' .. h .. ':' .. x .. ':' .. y .. ' -y ' .. imagePath
    os.execute(exec)
    ngx.exit(ngx.OK)
else
    originImage = string.match(imagePath, "^(.+/%w+%.%w+)_%d+_%d+%.%w+$")
    if originImage ~= nil then
        local w = string.match(imagePath, "^.+/%w+%.%w+_(%d+)_%d+%.%w+$")
        local h = string.match(imagePath, "^.+/%w+%.%w+_%d+_(%d+)%.%w+$")
        local exec = ffmpeg .. ' -i ' .. originImage .. ' -s ' .. w .. '*' .. h .. ' -y ' .. imagePath
        os.execute(exec)
        ngx.exit(ngx.OK)
    else
        ngx.exit(ngx.HTTP_FORBIDDEN)
    end
end

