package com.engine.dataobject;

import java.util.Random;


public class StaticValues {

	static public String  GAME_STATUS_NOT_STARTED = "NOT_STARTED";
	static public String GAME_STATUS_STARTED = "STARTED";
	static public String GAME_STATUS_DONE = "DONE";
	static public String GAME_STATUS_BETTING = "BETTING";
	public static  int TOTALPLAYERS = 21;
	static public  int SCOREPERPLAYER = 25;
	public static String BETTING_TYPE_OPEN = "OPEN";
	public static String BETTING_TYPE_CLOSED = "CLOSED";
	
	//Return code
	public static int CODE_SUCCESS = -5000;	
	public static int CODE_FAILURE = -5001;
	public static int CODE_SQL_FAILURE = -5002;
	public static int CODE_INVALID_BET = -5003;
	public static int CODE_DUPLICATE_ENTRY = -5555;
	public static int CODE_DATA_DOESNT_EXIST = -5004;
	
	
	//Strings to users!!
	public static String STR_BET_ALREADY_PLACED[] = {	"Thyaaannnx!!! mosa maadbedle kadima!! bet already placed",
														"Hum angrezz ke zamane ke jailor thei.. chor!!",
														"What you doing I say? bet already placed",
														"Noorondhu baigula .. Yedeaaladinda haadagi Bantu aanandadinda ...",
														"Yaarige beko eee bettu ... Yarige beko nin bettu ... Mosakke Kai mugibeka?? Chintheyu iddare nagabeka?? IPL sotharu irabeka .....",
														"Kudiyodhe nanna weaknessu aadare mosa thadiyodhe nanna businessu..."};
	
	public static String STR_MESSAGE_UNREADABLE[] = {	"Dont kill the bot with your nonsense, bet correctly please",
														"Excuse moi! serige bet hakla le!!!",
														"U r pushing it too far - Ab ki Baar Modi ki Sarkar..",
														"Dude! I dont understand what you say, bet correctly please",
														"Stop this emotional Atyachaar!! nahiiiin!!..",
														"Did you say hmmm? bet correctly please",
														"Adyen helithyo ninge gothu, bet correctly please",
														"Swalpa correctagi bet maadri!!",
														"Anisuthide yaako Indu .. Neenene nanavanendhu... Aaha yentha madhura yochne...",
														"With great power comes great responsibility.. oye spiderman.. type responsibly!!!",
														"Main tera khoon pijaunga...",
														"Arey ooo Samaba... kya kar rahe ho...",
														"Tera kya hoga Kaalia?",
														"Utamamm dadh dhadhatt padham ... madhyam padham thuchuk thuchuk ... khanishtham thudthudiiy padham ... sursuria pran ghatkam",
														"Jahapanah tussi great ho ... tofa kubool karo .. (_x_)",
														"Madhuri ki muskaan, Juhi ka jadoo, Sridevi ki shararat ...Aur tumahare nakre... wah!!",
														"Life alli ee levalige confuse aagidhu idhe modhlu....yella nim ashirvadha.........",
														"Yeno hero, life alli modalne saari BET hakidya, adrallu spelling mistake a a a??? karma kaanda.... ",
														"neevu wrong BET hakdidre nove aagathri, adhre ee novallu onthara sukha ide... sweet pain...sweet memories, jothege ee hot drink local.......neera kai allidhre doosra matter-E illa kanri......",
														"Artha aaglilla , aagodhu beda bidi..... ",
														"maLe joru huyta untuthandi chaLi pet kotre novaagtade.. Bet serighakla..",
														"maana, maryaade andre enu anta gottidiya nimge... ello keldangide.. asht idea illa ..",
														"grrr...you will lose ur bet today..haavina rosha hanerdu varusha.. Botappana dwesha 100 varusha..",
														"bhagvantaa kai kotta type madokkantha.. adrallu mistake yako madakathi...",
														"Kuchiku kuchiku kuchiku.. neenu chaddi dosthu kano kuchiku..O maraya yenmaadthiya...",
														"Aadisi nodu.. beelisi nodu.. urali hogadhu.. Botappa rocks!!",
														"laka laka laka laka laka.. motte BOSS... yenna pandre..",
														"wen Botappa becomes violent.. u become silent...",
														"oh shit... u fail in betting maam.. its unpossible..",
														"betting alli iro sukha gothe irlilla.. uoon anthiya.. uoonhoo anthiya..",
														"ayioo bejaar madkobedi seethapathi...",
														"Kariya I Love You.. Karunaad melaane... :-* ",
														"Kannada namamma.. Englishu maramma... nin BETu hogeyamma...",
														"Meri sapanoo ki raani kab ayegi tu... yeh bulbul BET hakakilva....",
														"aden hudgiro adhyaking aadthiro... ",
														"naa oru dhadave sonna.. noor dhadave sonna maadiri.... bet sariyama poodu...",
														"All izz well ... All izz well",
														};
	
	public static String STR_MESSAGE_CLOSEDBETTING1[] = {	"Aithalakadi jumma!! Closed betting appa idu ;)",
															"Assay Nodu!! Closed betting kanri- amele betting details thoristhivi",
															"Ree , nivu bet kelidaga Tumba chenagi Kanustira kanri :P",
															"Ree bet hakirodhu Nivala Nanu, Inmele Eege Tight agi idbidbeku kanri",
															"Ree Inta ole samayadali BET bage yakri matadtira, ree BET anodu paka 420",
															"Aha!! Paarvathi... nodu ninna maklu BET kelthidarre..",
															"Oh ho haagenu bahala santosha, Alrite.. Naale ba.",
															"Kaantha.. bet beka ninge kaantha bettu",
															"Yaare Koogadali, oore horadali... yamme ninge bettu thorsodilla :D",
															"Bachcha dheeraj rako, dheeraj... kamyabi to sali jhak maar ke peeche bhagegi",
															"Jis school mein tune ye sab seekha hai na ... uska headmaster aaj bhi mujhse tuition leta hai",
															"khoobsurat ladkiyan jab jhooth bolti hai ... toh aur bhi khoobsurat lagti hai",
															"aden hudgirooo... adhyaking aadthiro... betu betu antha kaat kodthiro....."};
	
	public static String STR_MESSAGE_CLOSEDBETTING[] = { "Ashwin bet haako.. yake beka bisi bisi kajjaya",
														"Ashwin ge ugiri swalpa, bet hako andre nakra maadthane..",
														"Lei Bhairava Ashwin Maan Singh Bet hakoo!!",
														"Ashwin pardesi... naatka adbedvo...sumne maryadhi agi bet haaku..",
														" Ashwin ****** &$^%(#( *@&^$&#* *@(#*** beep beep beep",
};
	
	public static String STR_SCORE_NOT_AVAILABLE[] = {	"vasi thadko Bhairava.. Match innu start agilla..",
														"my child..have patience in life...Let the match start.. ",
														"Shivane shoumbulinga....aathra padbedvo maraya.. match shuru aagli thadila..",
														"ee preethi..prema..scoreu.. yella pusthakad badnekaye..hogi kelsa maadkoli..",
														"MaanSingh...urgent jaasthi ninge...match start agli iro.."};
	
	public static String STR_SUCCESS_BET_PLACED = "Good Job!";
	public static String STR_FAILURE = "Something went wrong we will be right back!";
	public static String STR_INVALID_BET = "This is an invalid bet!";
	public static String STR_INDIVIDUAL_BET = "Thanks for placing your bet. Bet details will be released when betting completes.";
	public static String STR_THANK_YOU = " has placed the bets. Remaining folks please place your bets.";
	public static String STR_CLOSED_BETTING_MSG = "This is a CLOSED betting game. Please send your bets to this number +1-6502292341 from Whatsapp. Don't type your bets here.";
	
	
	//Return Error Messages
	/*public static int BET_ALREADY_PLACED_COUNT =2;
	public static String STR_BET_ALREADY_PLACED_0 = "Thyaaannnx!!! mosa maadbedle kadima!! bet already placed";
	public static String STR_BET_ALREADY_PLACED_1 = "What you doing I say? bet already placed";
	*/
	
	
	
	/*public static int MESSAGE_UNREADABLE_COUNT = 6;
	public static String STR_MESSAGE_UNREADABLE_0 = "Dont kill the bot with your nonsense, bet correctly please";
	public static String STR_MESSAGE_UNREADABLE_1 = "Excuse moi! serige bet hakla le!!!";
	public static String STR_MESSAGE_UNREADABLE_2 = "Dude! I dont understand what you say, bet correctly please";
	public static String STR_MESSAGE_UNREADABLE_3 = "Did you say @#$@#$@#%? bet correctly please";
	public static String STR_MESSAGE_UNREADABLE_4 = "Adyen helithyo ninge gothu, bet correctly please";
	public static String STR_MESSAGE_UNREADABLE_5 = "Swalpa correctagi bet maadri!!";
	*/
	
	
	public static void main(String [] s){
		int index = new Random().nextInt(StaticValues.STR_MESSAGE_UNREADABLE.length);
		System.out.println(StaticValues.STR_MESSAGE_UNREADABLE.length);
		System.out.println(index);
		String meanMsg = StaticValues.STR_MESSAGE_UNREADABLE[index];	 
		System.out.println(" hehe !! "+ meanMsg);
	}
}
