var walk = require('walkdir');
const fs = require('fs');
const walkSync = require('walk-sync');
let result = 0;
counted_files = 0;
files_number =0;

start = 0;
function getlines1(file,shared_callback){
    let count = 0;
    fs.createReadStream(file).on('data', function(chunk) {
        count += chunk.toString('utf8')
        .split(/\r\n|[\n\r\u0085\u2028\u2029]/g)
        .length-1;
    }).on('end', function() {
        shared_callback(count);
    }).on('error', function(err) {
        console.error(err);
    });
}

function asynchrononous(path){
    walker = walk.walk(path);
    walker.on("file",function(root){
        files_number +=1;
        getlines1(root,(line_counter)=>{
            counted_files += 1;
            result += line_counter;
            if(files_number == counted_files){
                console.log(result + " lines   "+(new Date()-start)+"ms");
            }
        })
    })
};

function synchronomous(path){
    paths = walkSync(path);
    file_ind = [];
    for(let i=0;i<paths.length;i++){
        try{
            if(fs.lstatSync(path+"/"+paths[i]).isFile()){
                file_ind.push(i);
            }
        }
        catch(e){
            if(e.code == 'ENOENT'){
            }
        }
    }

    let result_function = (result,i)=>{
        if(i == file_ind.length){
            console.log(result+" files "+(new Date()-start)+"ms");
            return;
        }
        getlines1(path+"/"+paths[file_ind[i]],(line_counter)=>result_function(result+line_counter,i+=1))
    }
    result_function(0,0)
}
start = new Date();
// asynchrononous("./node_modules")
synchronomous("./node_modules")