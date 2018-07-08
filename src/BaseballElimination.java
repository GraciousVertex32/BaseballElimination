import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileReader;
import java.util.Map;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;

public final class BaseballElimination
{
    private final int nots;
    private final int[] wins;
    private final int[] losses;
    private final int[] reamins;
    private final int[][] against;
    private final Map<String, Integer> NameToIndex = new HashMap<>();
    // temp

    public BaseballElimination(String filename) throws Exception        // create a baseball division from given filename in format specified below
    {
        File f = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(f));
        nots = Integer.parseInt(br.readLine());
        wins = new int[nots];
        losses = new int[nots];
        reamins = new int[nots];
        against = new int[nots][nots];
        String line;
        int count = 0;
        while ((line = br.readLine()) != null)
        {
            String[] temp = line.split(" +");
            //set basic value
            NameToIndex.put(temp[0], count);
            wins[count] = Integer.parseInt(temp[1]);
            losses[count] = Integer.parseInt(temp[2]);
            reamins[count] = Integer.parseInt(temp[3]);
            //set value in against
            for (int i = 0; i < nots; i++)
            {
                if (count != i)
                {
                    against[count][i] = Integer.parseInt(temp[i + 4]);
                }
            }
            count++;
        }
    }

    public int numberOfTeams()                        // number of teams
    {
        return nots;
    }

    public Iterable<String> teams()                                // all teams
    {
        return NameToIndex.keySet();
    }

    public int wins(String team)                      // number of wins for given team
    {
        return wins[NameToIndex.get(team)];
    }

    public int losses(String team)                    // number of losses for given team
    {
        return losses[NameToIndex.get(team)];
    }

    public int remaining(String team)                 // number of remaining games for given team
    {
        return reamins[NameToIndex.get(team)];
    }

    public int against(String team1, String team2)    // number of remaining games between team1 and team2
    {
        return against[NameToIndex.get(team1)][NameToIndex.get(team2)];
    }

    public boolean isEliminated(String team)              // is given team eliminated?
    {
        FlowNetwork net = Network(team);
        FordFulkerson fulkerson = new FordFulkerson(net, 0, net.V() - 1);
        int max = AllGames(team);
        return max != fulkerson.value();
    }

    public Iterable<String> certificateOfElimination(String team)  // subset R of teams that eliminates given team; null if not eliminated
    {
        FlowNetwork net = Network(team);
        FordFulkerson fulkerson = new FordFulkerson(net, 0, net.V() - 1);
        int max = AllGames(team);
        if (max != fulkerson.value())
        {
            int[] eliminations;
            for (FlowEdge edge: net.edges())
            {
                if (edge.from() == 0 && edge.flow() < edge.capacity())
                {

                }
            }
        }
        else
        {
            return null;
        }
    }

    private FlowNetwork Network(String team)
    {
        int GivenTeam = NameToIndex.get(team);
        int teams = against.length; // number of teams(include current)
        int pairs = (int) (Math.pow(teams, 2) - teams) / 2; // number of pairs(include current)
        int maxwin = wins[GivenTeam] + reamins[GivenTeam]; // available wins for other teams
        FlowNetwork net = new FlowNetwork(pairs + teams + 2);
        int counter = 1; //
        for (int current = 0; current < teams; current++)
        {
            for (int others = current + 1; others < teams; others++) // each pair
            {
                if (current != GivenTeam && others != GivenTeam)// avoid test team
                {
                    net.addEdge(new FlowEdge(0, counter, against[current][others]));//s to paris
                    net.addEdge(new FlowEdge(counter, 1 + pairs + current, Integer.MAX_VALUE)); // pairs to result
                    net.addEdge(new FlowEdge(counter, 1 + pairs + others, Integer.MAX_VALUE)); // pairs to result
                }
                counter++; // for pair placement
            }
            if (current != GivenTeam)
            {
                net.addEdge(new FlowEdge(1 + pairs + current, pairs + teams + 1, maxwin - wins[current])); // result to t
            }
        }
        return net;
    }

    private int AllGames(String name)
    {
        int GivenTeam = NameToIndex.get(name);
        int NoT = against.length;
        int games = 0;
        for (int current = 0; current < NoT; current++)
        {
            for (int others = current + 1; others < NoT; others++) // each pair
            {
                if (current != GivenTeam && others != GivenTeam)
                {
                    games+= against[current][others];
                }
            }
        }
        return games;
    }

    public static void main(String[] args)
    {
        try
        {
            BaseballElimination division = new BaseballElimination("teams4.txt");

            for (String team : division.teams())
            {
                if (division.isEliminated(team))
                {
                    StdOut.print(team + " is eliminated by the subset R = { ");
                    for (String t : division.certificateOfElimination(team))
                    {
                        StdOut.print(t + " ");
                    }
                    StdOut.println("}");
                } else
                {
                    StdOut.println(team + " is not eliminated");
                }
            }

        }
        catch (Exception e)
        {

        }
    }
}
