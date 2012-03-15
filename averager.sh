# USAGE: averager.sh COMMAND NUM
#        COMMAND   - the command to execute, should output 
#                    "x total milliseconds" somewhere
#        NUM       - number of times to run

CMD=$1
NUM=$2

i=0
total=0
while [ $i -lt $NUM  ]; do
    #execute command, get number
    TIME=`eval $CMD | grep -E '[0-9]+ total milliseconds' | cut -f 1 -d ' '`
    #add to sum
    echo "$i : $TIME"
    let total=total+TIME
    let i=i+1
done

#print average
let total=total/NUM
echo "Average:  $total"