#!/usr/bin/env bash

if [ -z ${RISCV+x} ]; then
    echo "The RISCV environment variable is not set. Please set it and rerun script."
    exit 1
fi

# Create log folder
mkdir -p logs

TEST_FILE_FOLDER=Adept-TestFiles/instructions
HEXS=$TEST_FILE_FOLDER/hex
RESULTS_FOLDER=$TEST_FILE_FOLDER/results/
TOP=$(pwd)

cd $TEST_FILE_FOLDER && make clean && make
cd $TOP

for test in $(ls $HEXS); do
    LOG_FOLDER=logs/"$test"_log
    mkdir -p $LOG_FOLDER

    # Run test in verilator
    make test-verilator PROG=$HEXS/$test > $LOG_FOLDER/verilator_$test_$(date +%d-%m-%Y)
    # Cut the log and take just the output that we want
    if grep -q 'Trap' $LOG_FOLDER/verilator_$test_$(date +%d-%m-%Y); then
	tac $LOG_FOLDER/verilator_$test_$(date +%d-%m-%Y) | grep "Trap" -m 1 -B33 | sed 's/\[.*\] \[.*\] //g' | tac > $LOG_FOLDER/verilator_result_$test_$(date +%d-%m-%Y)
    else
	tac $LOG_FOLDER/verilator_$test_$(date +%d-%m-%Y) | grep "PC = " -m 1 -B32 | sed 's/\[.*\] \[.*\] //g' | tac > $LOG_FOLDER/verilator_result_$test_$(date +%d-%m-%Y)
    fi
    diff -w $LOG_FOLDER/verilator_result_$test_$(date +%d-%m-%Y) $RESULTS_FOLDER/${test%.hex}/verilator

done
