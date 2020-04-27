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

export default function MatchTable(props) {
  const classes = useStyles();

  function toOddsStr(odds) {
    return "H:" + odds.H +
      "\nA:" + odds.A +
      "\nD:" + odds.D
  }
  
  return (
    <div>
      <TableContainer component={Paper}>
        <Table className={classes.table} size="small" aria-label="a dense table">
          <TableHead>
            <TableRow>
              <TableCell>Num</TableCell>
              <TableCell align="right">Date</TableCell>
              <TableCell align="right">League</TableCell>
              <TableCell align="right">Home</TableCell>
              <TableCell align="right">Away</TableCell>
              <TableCell align="right">Status</TableCell>
              <TableCell align="right">Last Update</TableCell>
              <TableCell align="right">Odds</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {props.matchList.map((m) => (
              <TableRow key={m.matchNum}>
                <TableCell component="th" scope="row">
                  {m.matchNum}
                </TableCell>
                <TableCell align="right">{m.matchDate.substring(0, 10)}</TableCell>
                <TableCell align="right">{m.league.leagueShortName}</TableCell>
                <TableCell align="right">{m.homeTeam.teamNameCH}</TableCell>
                <TableCell align="right">{m.awayTeam.teamNameCH}</TableCell>
                <TableCell align="right">{m.matchStatus}</TableCell>
                <TableCell align="right">{m.statuslastupdated.substring(0, 19)}</TableCell>
                <TableCell align="right">{toOddsStr(m.hadodds)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
}
