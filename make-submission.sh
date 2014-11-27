#/bin/bash

# Copy all the necessary files
mkdir -p submission
cp -r tests submission/.
cp -r tool submission/.
cp -r testerpy submission/.
cp report.pdf submission/.
cp run_comp.sh submission/.
cp run_tests.sh submission/.
cp init.sh submission/.

# Make the archive
tar -cvzf SRToolSubmission.tgz .

# Delete the folder
rm -rf submission

echo "done"
