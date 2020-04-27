import React, { useState } from "react";
import Box from '@material-ui/core/Box';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import Alert from '@material-ui/lab/Alert';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles((theme) => ({
  button: {
    '& > *': {
      margin: theme.spacing(1),
    },
  },
}));

export default function LoginCtrl(props) {
  const classes = useStyles();

  var [host, setHost] = useState(props.host);
  var [port, setPort] = useState(props.port);
  var [userName, setUserName] = useState(props.userName);
  var [password, setPassword] = useState(props.password);

  return (
      <Box>
        <Alert variant="filled" severity={props.status.isConnect ? "success" : "warning"}>{props.status.text}</Alert>
        <br />
        <form noValidate autoComplete="off">
          <div>
            <TextField required id="host" label="Host" value={host} onChange={e => setHost(e.target.value)} />
            <TextField required id="port" label="Port" value={port} onChange={e => setPort(e.target.value)} />
            <TextField required id="userName" label="UserName" value={userName} onChange={e => setUserName(e.target.value)} />
            <TextField required id="password" label="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} />
          </div>
          <div className={classes.button}>
          <Button variant="contained" color="primary" onClick={()=>props.connect(host,port,userName,password)}
            disabled={props.status.isConnect}>Connect</Button>
          <Button variant="contained" color="primary" onClick={props.disConnect}
            disabled={!props.status.isConnect}>Disconnect</Button>
          </div>
        </form>
      </Box>
  );
}
