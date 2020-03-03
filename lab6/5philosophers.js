var Fork = function(id) {
    this.state = 0;
    this.id = id;
    return this;
}

Fork.prototype.acquire = function(cb) { 

    var getFork = (cb, attempt, currentDelay, totalWaitTime) => {
        setTimeout(()=>{
            if(this.state == 0){
                // console.log(this.id+"is being used");
                this.state = 1;
                cb(totalWaitTime);
            }else{
                // console.log(this.id+" is waiting");
                maxWTime = 2^attempt-1;
                nextDelay = Math.floor(Math.random()*(maxWTime+1));
                getFork(cb,attempt+1,nextDelay ,totalWaitTime + nextDelay);
            }
        }, currentDelay);
    };
    getFork(cb,1,1,1);
}

Fork.prototype.acquire2 = function(cb) { 

    var getFork = (cb, attempt, currentDelay, totalWaitTime) => {
        setTimeout(()=>{
            if(this.state == 0){
                // console.log(this.id+"is being used");
                Conductor.queue.push(this);
                this.state = 1;
                cb(totalWaitTime);
            }else{
                // console.log(this.id+" is waiting");
                maxWTime = 2^attempt-1;
                nextDelay = Math.floor(Math.random()*(maxWTime+1));
                getFork(cb,attempt+1,nextDelay ,totalWaitTime + nextDelay);
            }
        }, currentDelay);
    };
    getFork(cb,1,1,1);
}



Fork.prototype.release = function() { 
    this.state = 0;
}

var Philosopher = function(id, forks) {
    this.id = id;
    this.forks = forks;
    this.f1id = id % forks.length;
    this.f2id = (id+1) % forks.length;
    this.f1 = forks[id % forks.length];
    this.f2 = forks[(id+1) % forks.length];
    return this;
}

Philosopher.prototype.acquireBothForks = function(cb){
    var getFork = (cb, attempt, currentDelay, totalWaitTime) => {
        setTimeout(()=>{
            if(this.f1.state == 0 && this.f2.state == 0){
                // console.log(this.id+"is being used");
                this.f1.state = 1;
                this.f2.state = 1;

                cb(totalWaitTime);
            }else{
                // console.log(this.id+" is waiting");
                maxWTime = 2^attempt-1;
                nextDelay = Math.floor(Math.random()*(maxWTime+1));
                getFork(cb,attempt+1,nextDelay ,totalWaitTime + nextDelay);
            }
        }, currentDelay);
    };
    getFork(cb,1,1,1);
}


Philosopher.prototype.startNaive = function(count) {
    var f1 = this.f1,
        f2 = this.f2,
        id = this.id;
    
    if(count == 0){
        console.log(id+","+parseInt(times.get(id)/N)+","+version+","+N);
        return;
    }

    setTimeout(()=>{
        f1.acquire((wTime1)=>{
            f2.acquire((wTime2)=>{
            setTimeout(()=>{
                // console.log(id+" is eating");
                // console.log(wTime1,wTime2,id);
                times.set(id,times.get(id)+wTime1+wTime2);
                f1.release();
                f2.release();
                // console.log(id+" is done eating");
                this.startNaive(count-1);
            },200);
       }); 
    }); 
    },Math.floor(Math.random()*200));
    
}

Philosopher.prototype.startAsym = function(count) {
    var f1 = (this.id % 2 == 0)?this.f2:this.f1,
    f2 = (this.id % 2 == 0)?this.f1:this.f2,
    id = this.id;

    if(count == 0){
        console.log(id+","+parseInt(times.get(id)/N)+","+version+","+N);
        return;
    }
    setTimeout(()=>{
        f1.acquire((wTime1)=>{
            f2.acquire((wTime2)=>{
            setTimeout(()=>{
                // console.log(id+" is eating");
                // console.log(wTime1,wTime2,id);
                times.set(id,times.get(id)+wTime1+wTime2);
                f1.release();
                f2.release();
                // console.log(id+" is done eating");
                this.startAsym(count-1);
            },200);
       }); 
    }); 
    },Math.floor(Math.random()*200));
    

}
var Conductor = function(){
    this.queue = [];
    return;
}
Conductor.prototype.permission = function(philo,cb){
    if(this.queue.length == 0){
        cb(true);
        return;
    }
    ForksReserved = {}
    for (i =0;i<N;i++){
        ForksReserved[i] = false;
    }
    if(this.queue.includes(philo)){
        this.queue.forEach(function(element){
            // console.log(philo.id,element.id);
        if(philo.id == element.id && ForksReserved[philo.f1id] == false && ForksReserved[philo.f2id] == false){
            // console.log(philo.id,philo.f1.state,philo.f2.state);
            setTimeout(()=>{
                cb(true);
                return;
            },0);
        }
        ForksReserved[element.f1id] = true;
        ForksReserved[element.f2id] = true;
    })
        cb(false);
        return;
    }else{
        this.queue.forEach(function(element){
            ForksReserved[element.f1id] = true;
            ForksReserved[element.f2id] = true;
        })
        if(ForksReserved[philo.f1id] == false && ForksReserved[philo.f2id] == false){
            setTimeout(function(result){
                cb(true);
                return;
            },0);
        }
        cb(false);
        return
    }
    

}
Count2 = 0;
Conductor.prototype.acquire = function(philo,cb) { 
    var getForks = (cb, attempt, currentDelay, totalWaitTime) => {
        setTimeout(()=>{

            if(philo.f1.state == 0 && philo.f2.state == 0){
                // console.log(philo.id)
                this.permission(philo,(permis)=>{
                    if(permis){
                        // if(philo.id == 0 )console.log("halo")
                        // console.log(philo.id+" is being used");
                        philo.f1.state = 1;
                        philo.f2.state = 1;
                        this.queue = this.queue.filter(function(el){return el.id != philo.id})
                        // console.log(totalWaitTime,philo.id)
                        cb(totalWaitTime);
                        return;
                    }
                    else{
                        if(!this.queue.includes(philo)){
                            // console.log("adding to queue "+ philo.id)
                            this.queue.push(philo);
                        }
                        // this.queue.forEach(elem => console.log(elem.id))
                        // console.log(philo.id+" is waiting");
                        maxWTime = 2^attempt-1;
                        nextDelay = Math.floor(Math.random()*(maxWTime+1));
                        // console.log(nextDelay,philo.id)
                        getForks(cb,attempt+1,nextDelay ,totalWaitTime + nextDelay);
                    }
                    
                });
            }else{
                if(!this.queue.includes(philo)){
                    this.queue.push(philo);
                }
                // this.queue.forEach(elem => console.log(elem.id))
                // console.log(philo.id+" is waiting");
                maxWTime = 2^attempt-1;
                nextDelay = Math.floor(Math.random()*(maxWTime+1));
                getForks(cb,attempt+1,nextDelay ,totalWaitTime + nextDelay);
            }
        }, currentDelay);
    };
    getForks(cb,1,1,1);
}
Philosopher.prototype.startConductor = function(count,cb) {
    var forks = this.forks,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id;
    
    if(count == 0){
        // times.forEach(element => console.log(element));
        console.log(id+","+parseInt(times.get(id)/N)+","+version+","+N);
        // cb(String(id+","+parseInt(times.get(id)/N)+","+version+","+N));
        return;
    }
    // if(this.id == 0)console.log(Count2++,count)
    setTimeout(()=>{
        conductor.acquire(this,(wTime1)=>{
            setTimeout(()=>{
                // console.log(id+" is eating");
                // console.log(wTime1,id);
                times.set(id,times.get(id)+wTime1);
                
                f1.release();
                f2.release();
                // console.log(id+" is done eating");
                this.startConductor(count-1);
            },200);
    }); 
    },Math.floor(Math.random()*200));

}

Philosopher.prototype.startSimult = function(count) {
    var f1 = this.f1,
        f2 = this.f2,
        id = this.id;
    
    if(count == 0){
        console.log(id+","+parseInt(times.get(id)/N)+","+version+","+N);
        return;
    }

    setTimeout(()=>{
        this.acquireBothForks((wTime) =>{
            setTimeout(()=>{
                // console.log(id+" is eating");
                // console.log(wTime1,wTime2,id);
                times.set(id,times.get(id)+wTime);
                f1.release();
                f2.release();
                // console.log(id+" is done eating");
                this.startSimult(count-1);
            },200);
        
       }); 
    },Math.floor(Math.random()*200)); 
}
var times = new Map()
var N = 5;
var version = "a";
var forks = [];
var philosophers = []
var conductor = new Conductor();
for (var i = 0; i < N; i++) {
    forks.push(new Fork(i));
    times.set(i,0);
}

for (var i = 0; i < N; i++) {
    philosophers.push(new Philosopher(i, forks));
}

for (var i = 0; i < N; i++) {
    switch(version){
        case "n":
            philosophers[i].startNaive(10);
            break;
        case "a":
            philosophers[i].startAsym(10);
            break;
        case "c":
            conductor.queue = conductor.queue.filter(function(el){return el.id != 0})
            philosophers[i].startConductor(10,(output)=>{
                console.log(output);
            });
            break;
        case "s":
            philosophers[i].startSimult(10);
            break;
    }
}