local imagePath = ngx.var.imagePath;
local ffmpeg = ngx.var.ffmpeg;
local directoryPath = string.match(imagePath, "^(.+)/%d+_%d+_%d+_%d+%.png$")
if directoryPath ~= nil then
    local x = tonumber(string.match(imagePath, "^.+/(%d+)_%d+_%d+_%d+%.png$"))
    local y = tonumber(string.match(imagePath, "^.+/%d+_(%d+)_%d+_%d+%.png$"))
    local w = tonumber(string.match(imagePath, "^.+/%d+_%d+_(%d+)_%d+%.png$"))
    local h = tonumber(string.match(imagePath, "^.+/%d+_%d+_%d+_(%d+)%.png$"))
    local mr = io.popen(ffmpeg .. ' -i ' .. directoryPath .. '/o.png 2>&1')
    local result = mr:read("*all")
    local width = tonumber(string.match(result, "Stream.*[^%d]+(%d+)x%d+[^%d]+"))
    local height = tonumber(string.match(result, "Stream.*[^%d]+%d+x(%d+)[^%d]+"))
    if (x + w) > width or (y + h) > height then
        --截图宽度超出图片的总宽度或者截图高度超出图片的总高度
        ngx.exit(ngx.HTTP_FORBIDDEN)
    end

    local exec = ffmpeg .. ' -i ' .. directoryPath .. '/o.png -vf crop=' .. w .. ':' .. h .. ':' .. x .. ':' .. y .. ' -y ' .. imagePath
    os.execute(exec)
    ngx.exit(ngx.OK)
else
    directoryPath = string.match(imagePath, "^(.+)/%d+_%d+%.png$")
    if directoryPath ~= nil then
        local w = string.match(imagePath, "^.+/(%d+)_%d+%.png$")
        local h = string.match(imagePath, "^.+/%d+_(%d+)%.png$")
        local exec = ffmpeg .. ' -i ' .. directoryPath .. '/o.png -s ' .. w .. '*' .. h .. ' -y ' .. imagePath
        os.execute(exec)
        ngx.exit(ngx.OK)
    else
        ngx.exit(ngx.HTTP_FORBIDDEN)
    end
end

