import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';

const useStyles = makeStyles({
  table: {
    minWidth: 650,
  },
});

export default function DeltaTable(props) {
  const classes = useStyles();

  function toOddsStr(odds) {
    return "H:" + odds.H +
      ", A:" + odds.A +
      ", D:" + odds.D
  }
  
  return (
    <div>
      <TableContainer component={Paper}>
        <Table className={classes.table} size="small" aria-label="a dense table">
          <TableHead>
            <TableRow>
              <TableCell>Sequence</TableCell>
              <TableCell>Num</TableCell>
              <TableCell align="right">Status</TableCell>
              <TableCell align="right">Last Update</TableCell>
              <TableCell align="right">Odds</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {props.deltaList.map((d) => (
              <TableRow key={d.sequenceInt}>
                <TableCell component="th" scope="row">{d.sequenceInt}</TableCell>
                <TableCell align="right">{d.matchNum}</TableCell>
                <TableCell align="right">{d.matchStatus}</TableCell>
                <TableCell align="right">{d.statuslastupdated.substring(0, 19)}</TableCell>
                <TableCell align="right">{toOddsStr(d.hadodds)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
}
