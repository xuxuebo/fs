local imagePath = ngx.var.imagePath;
local ffmpeg = ngx.var.ffmpeg;
local originImage = string.match(imagePath, "^(.+/%w+%.%w+)_%d+_%d+_%d+_%d+%.%w+$")
if originImage ~= nil then
    local x = string.match(imagePath, "^.+/%w+%.%w+_(%d+)_%d+_%d+_%d+%.%w+$")
    local y = string.match(imagePath, "^.+/%w+%.%w+_%d+_(%d+)_%d+_%d+%.%w+$")
    local w = string.match(imagePath, "^.+/%w+%.%w+_%d+_%d+_(%d+)_%d+%.%w+$")
    local h = string.match(imagePath, "^.+/%w+%.%w+_%d+_%d+_%d+_(%d+)%.%w+$")
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

