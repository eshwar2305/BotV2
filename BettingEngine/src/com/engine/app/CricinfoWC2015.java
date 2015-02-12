package com.engine.app;

import com.engine.dataobject.Game;
import com.engine.dataobject.StaticValues;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

// Referenced classes of package com.engine.app:
//            CricinfoListener

public class CricinfoWC2015 extends Thread
{

    public void addCricinfoListener(CricinfoListener cc)
    {
        m_listeners.add(cc);
    }

    public void removeCricinfoListener(CricinfoListener cc)
    {
        m_listeners.remove(cc);
    }

    protected void fireMatchResultAvailable(Game g)
    {
        CricinfoListener cc;
        for(Iterator i = m_listeners.iterator(); i.hasNext(); cc.fireMatchResultAvailable(g))
            cc = (CricinfoListener)i.next();

    }

    public void run()
    {
        do
        {
            for(; isMatchComplete(m_onGoingMatchNumber); m_onGoingMatchNumber++)
            {
                Game game = new Game();
                game.setGameId(m_onGoingMatchNumber);
                game.setWinTeam(getMatchWinner(m_onGoingMatchNumber));
                game.setTeamA(getTeamA(m_onGoingMatchNumber));
                game.setTeamB(getTeamB(m_onGoingMatchNumber));
                System.out.println((new StringBuilder("In Cricinfo - Match number =")).append(m_onGoingMatchNumber).append(" results available").toString());
                fireMatchResultAvailable(game);
            }

            System.out.println("Cricinfo - Sleeping for 10 mins");
            try
            {
                sleep(m_sampleInterval);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            updateCricInfoDetails();
        } while(true);
    }

    public CricinfoWC2015(String url)
    {
        m_sampleInterval = 0x927c0L;
        m_onGoingMatchNumber = 1;
        m_listeners = new HashSet();
        m_url = url;
        updateCricInfoDetails();
    }

    public void updateCricInfoDetails()
    {
        System.out.println("Updating CricInfo Details");
        try
        {
            Document doc = Jsoup.connect(m_url).timeout(6000).get();
            m_potClass = doc.getElementsByClass("potMatchLink");
            for(int i = m_potClass.size() - 1; i > 0; i -= 2)
                m_potClass.remove(i);

            m_potScores = doc.getElementsByClass("show-for-small");
            m_potStatus = doc.getElementsByClass("show-for-small");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public String getMatchInfo(int i)
    {
        StringBuffer sb = new StringBuffer();
        Element match = m_potClass.get(i - 1);
        Element score = m_potScores.get(i - 1);
        Element status = m_potStatus.get(i - 1);
        TextNode matchNumber = (TextNode)match.textNodes().get(0);
        TextNode matchDate = (TextNode)match.textNodes().get(1);
        TextNode matchDesc = (TextNode)match.child(0).child(0).textNodes().get(0);
        List matchScoreNodes = score.childNodes();
        String matchScoreStr;
        if(matchScoreNodes.size() != 0)
        {
            TextNode matchScore = (TextNode)matchScoreNodes.get(0);
            matchScoreStr = matchScore.text();
        } else
        {
            matchScoreStr = "N/A";
        }
        List childElements = status.children();
        String matchStatusStr;
        if(childElements.size() == 0)
        {
            List matchStatusNodes = status.textNodes();
            if(matchStatusNodes.size() != 0)
            {
                TextNode matchStatus = (TextNode)matchStatusNodes.get(0);
                matchStatusStr = matchStatus.text();
            } else
            {
                matchStatusStr = "N/A";
            }
        } else
        {
            Element childEle = (Element)childElements.get(0);
            List matchStatusNodes = childEle.textNodes();
            if(matchStatusNodes.size() != 0)
            {
                TextNode matchStatus = (TextNode)matchStatusNodes.get(0);
                matchStatusStr = matchStatus.text();
            } else
            {
                matchStatusStr = "N/A";
            }
        }
        sb.append(matchDate.text()).append(matchNumber.text()).append(matchDesc.text()).append(matchScoreStr).append(matchStatusStr).append("\n");
        return sb.toString();
    }

    public String printCompleteSchedule()
    {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < m_potClass.size(); i++)
        {
            Element match = m_potClass.get(i);
            Element score = m_potScores.get(i);
            Element status = m_potStatus.get(i);
            TextNode matchNumber = (TextNode)match.textNodes().get(0);
            TextNode matchDate = (TextNode)match.textNodes().get(1);
            TextNode matchDesc = (TextNode)match.child(0).child(0).textNodes().get(0);
            List matchScoreNodes = score.childNodes();
            String matchScoreStr;
            if(matchScoreNodes.size() != 0)
            {
                TextNode matchScore = (TextNode)matchScoreNodes.get(0);
                matchScoreStr = matchScore.text();
            } else
            {
                matchScoreStr = "N/A";
            }
            List childElements = status.children();
            String matchStatusStr;
            if(childElements.size() == 0)
            {
                List matchStatusNodes = status.textNodes();
                if(matchStatusNodes.size() != 0)
                {
                    TextNode matchStatus = (TextNode)matchStatusNodes.get(0);
                    matchStatusStr = matchStatus.text();
                } else
                {
                    matchStatusStr = "N/A";
                }
            } else
            {
                Element childEle = (Element)childElements.get(0);
                List matchStatusNodes = childEle.textNodes();
                if(matchStatusNodes.size() != 0)
                {
                    TextNode matchStatus = (TextNode)matchStatusNodes.get(0);
                    matchStatusStr = matchStatus.text();
                } else
                {
                    matchStatusStr = "N/A";
                }
            }
            sb.append(matchDate.text()).append(matchNumber.text()).append(matchDesc.text()).append(matchScoreStr).append(matchStatusStr);
        }

        return sb.toString();
    }

    public boolean isMatchComplete(int i)
    {
        String str = getMatchStatus(i);
        return str.contains("No result") || str.contains("abandoned") || str.contains("tied") || str.contains("won by");
    }

    public String getMatchWinner(int i)
    {
        if(isMatchComplete(i))
        {
            String str = getMatchStatus(i);
            if(str.contains("won by"))
            {
                int index1 = str.indexOf("won by");
                if(str.contains("T20"))
                    index1 = str.indexOf("T20");
                str = str.substring(0, index1 - 1);
                return str.trim();
            }
            if(str.contains("tied"))
            {
                int index1 = str.indexOf("tied");
                if(str.contains("won"))
                {
                    int index2 = str.indexOf("won");
                    return str.substring(index1 + 5, index2 - 1);
                } else
                {
                    return "DRAW";
                }
            }
            if(str.contains("No result") || str.contains("abandoned"))
                return "DRAW";
        }
        return "Match Not Complete";
    }

    public String getMatchStatus(int i)
    {
        Element status = m_potStatus.get(i - 1);
        List childElements = status.children();
        String matchStatusStr;
        if(childElements.size() == 0)
        {
            List matchStatusNodes = status.textNodes();
            if(matchStatusNodes.size() != 0)
            {
                TextNode matchStatus = (TextNode)matchStatusNodes.get(0);
                matchStatusStr = matchStatus.text();
            } else
            {
                matchStatusStr = "N/A";
            }
        } else
        {
            Element childEle = (Element)childElements.get(0);
            List matchStatusNodes = childEle.textNodes();
            if(matchStatusNodes.size() != 0)
            {
                TextNode matchStatus = (TextNode)matchStatusNodes.get(0);
                matchStatusStr = matchStatus.text();
            } else
            {
                matchStatusStr = "N/A";
            }
        }
        return matchStatusStr;
    }

    public String getMatchTeams(int i)
    {
        Element match = m_potClass.get(i - 1);
        TextNode matchDesc = (TextNode)match.child(0).textNodes().get(0);
        String str = matchDesc.text();
        int index = str.indexOf(" at ");
        str = str.substring(0, index);
        return str.trim();
    }

    public String getTeamA(int i)
    {
        String teamA = getMatchTeams(i);
        int index = teamA.indexOf(" v ");
        teamA = teamA.substring(0, index);
        return teamA.trim();
    }

    public String getTeamB(int i)
    {
        String teamB = getMatchTeams(i);
        int index = teamB.indexOf(" v ");
        teamB = teamB.substring(index + 2);
        return teamB.trim();
    }

    public String getPlace(int i)
    {
        Element match = m_potClass.get(i - 1);
        TextNode matchDesc = (TextNode)match.child(0).child(0).textNodes().get(0);
        String str = matchDesc.text();
        int index = str.indexOf(" at ");
        str = str.substring(index + 3);
        return str.trim();
    }

    public String getScoreSheet()
    {
        try
        {
            Document doc = Jsoup.connect("http://www.espncricinfo.com/").timeout(6000).get();
            m_potLiveScore = doc.getElementsByClass("scoreline-list");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        Elements scores = m_potLiveScore.get(0).getElementsByClass("espni-livescores-scoreline");
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < scores.size(); i++)
        {
            Element l1 = scores.get(i).child(0);
            Elements teamname = l1.getElementsByClass("team-name");
            Elements teamscore = l1.getElementsByClass("team-score");
            Elements starttime = l1.getElementsByClass("start-time");
            Element tname = teamname.get(0);
            Element tscore = teamscore.get(0);
            sb.append(tname.textNodes().get(0));
            if(!tscore.textNodes().isEmpty())
                sb.append(tscore.textNodes().get(0));
            sb.append(" vs ");
            tname = teamname.get(1);
            tscore = teamscore.get(1);
            sb.append(tname.textNodes().get(0));
            if(!tscore.textNodes().isEmpty())
                sb.append(tscore.textNodes().get(0));
            sb.append(" - ");
            tname = starttime.get(0);
            if(!tname.textNodes().isEmpty())
                sb.append(tname.textNodes().get(0));
            sb.append("\n");
            System.out.println();
        }

        return sb.toString().replaceAll("&nbsp;ov", "").replaceAll("&amp;", "");
    }

    public String getScoreSheet(int i)
    {
        Element score = m_potScores.get(i - 1);
        StringBuffer matchScoreStr = new StringBuffer();
        List matchScoreNodes = score.childNodes();
        if(matchScoreNodes.size() != 0)
        {
            TextNode matchScore = (TextNode)matchScoreNodes.get(0);
            matchScoreStr.append(matchScore.text().replace("T20 ", ""));
            matchScoreStr.append("\n");
            matchScoreStr.append(getMatchStatus(i).replace("T20 ", ""));
        } else
        {
            int index = (new Random()).nextInt(StaticValues.STR_SCORE_NOT_AVAILABLE.length);
            String meanMsg = StaticValues.STR_SCORE_NOT_AVAILABLE[index];
            matchScoreStr.append(meanMsg);
        }
        return matchScoreStr.toString();
    }

    public String getMatchDate(int i)
    {
        Element match = m_potClass.get(i - 1);
        TextNode matchDate = (TextNode)match.textNodes().get(1);
        String str = matchDate.text();
        str = str.substring(2);
        return str;
    }

    private long m_sampleInterval;
    private int m_onGoingMatchNumber;
    private String m_url;
    private Set m_listeners;
    Elements m_potClass;
    Elements m_potScores;
    Elements m_potStatus;
    Elements m_potLiveScore;
}