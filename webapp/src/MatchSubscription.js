import React, { useState } from "react";
import Box from '@material-ui/core/Box';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/core/styles';

import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormLabel from '@material-ui/core/FormLabel';

const useStyles = makeStyles((theme) => ({
  button: {
    '& > *': {
      margin: theme.spacing(1),
    },
  },
}));

export default function MatchSubscription(props) {
  const classes = useStyles();

  var [testCase, setTestCase] = useState(props.testCase);
  var [topics, setTopics] = useState(props.topicsMap.get(props.testCase));
  
  var disabledStart = true, disabledStop = true;
  if (props.status.isConnect) {
    disabledStart = props.isStart;
    disabledStop = !props.isStart;
  }

  function onCaseChange(value) {
    setTestCase(value)
    setTopics(props.topicsMap.get(value))
  }
  
  return (
    <Box my={1}>
      <form noValidate autoComplete="off">
      <FormLabel component="legend">Test Cases</FormLabel>
      <RadioGroup row aria-label="position" name="position"
        value={testCase} onChange={e => onCaseChange(e.target.value)}>
        <FormControlLabel
          value="FULL"
          control={<Radio color="primary" />}
          label="FULL"
          disabled={disabledStart}
        />
        <FormControlLabel
          value="DELTA"
          control={<Radio color="primary" />}
          label="DELTA"
          disabled={disabledStart}
        />
        <FormControlLabel
          value="SEQUENCE"
          control={<Radio color="primary" />}
          label="SEQUENCE"
          disabled={disabledStart}
        />
      </RadioGroup>
        <div>
          <TextField
            id="topics"
            label="Topics"
            style={{ margin: 8 }}
            placeholder="Placeholder"
            fullWidth
            margin="normal"
            value={topics}
            disabled
            onChange={e => setTopics(e.target.value)}
          />
        </div>
        <div className={classes.button}>
          <Button variant="contained" color="primary"
            disabled={disabledStart}
            onClick={()=>props.start(testCase)}>Start</Button>
          <Button variant="contained" color="primary"
            disabled={disabledStop}
            onClick={props.stop}>Stop</Button>
        </div>
      </form>
    </Box>
  );
}
