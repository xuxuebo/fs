local imagePath = ngx.var.imagePath;
local ffmpeg = ngx.var.ffmpeg;
local directoryPath = string.match(imagePath, "^(.+)/%d+_%d+_%d+_%d+%.png$")
if directoryPath ~= nil then
    local x = string.match(imagePath, "^.+/(%d+)_%d+_%d+_%d+%.png$")
    local y = string.match(imagePath, "^.+/%d+_(%d+)_%d+_%d+%.png$")
    local w = string.match(imagePath, "^.+/%d+_%d+_(%d+)_%d+%.png$")
    local h = string.match(imagePath, "^.+/%d+_%d+_%d+_(%d+)%.png$")
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

