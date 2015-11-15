package ma.cgi.gtc.spaich;

public class Jeton {
	
	// Définition des attributs
	  int numeroLigne;
	  String tokentype;
	  boolean estMotCle;
	  String corps;
	  boolean estCree;
	  boolean estMotCleOuvrir;
	  int niveau;
	  Object activite;
	  



	/**
	 * @return the numeroLigne
	 */
	public int getNumeroLigne() {
		return numeroLigne;
	}




	/**
	 * @param numeroLigne the numeroLigne to set
	 */
	public void setNumeroLigne(int numeroLigne) {
		this.numeroLigne = numeroLigne;
	}




	/**
	 * @return the tokentype
	 */
	public String getTokentype() {
		return tokentype;
	}




	/**
	 * @param tokentype the tokentype to set
	 */
	public void setTokentype(String tokentype) {
		this.tokentype = tokentype;
	}




	/**
	 * @return the estMotCle
	 */
	public boolean isEstMotCle() {
		return estMotCle;
	}




	/**
	 * @param estMotCle the estMotCle to set
	 */
	public void setEstMotCle(boolean estMotCle) {
		this.estMotCle = estMotCle;
	}




	/**
	 * @return the corps
	 */
	public String getCorps() {
		return corps;
	}




	/**
	 * @param corps the corps to set
	 */
	public void setCorps(String corps) {
		this.corps = corps;
	}




	/**
	 * @return the estCree
	 */
	public boolean isEstCree() {
		return estCree;
	}




	/**
	 * @param estCree the estCree to set
	 */
	public void setEstCree(boolean estCree) {
		this.estCree = estCree;
	}




	/**
	 * @return the estMotCleOuvrir
	 */
	public boolean isEstMotCleOuvrir() {
		return estMotCleOuvrir;
	}




	/**
	 * @param estMotCleOuvrir the estMotCleOuvrir to set
	 */
	public void setEstMotCleOuvrir(boolean estMotCleOuvrir) {
		this.estMotCleOuvrir = estMotCleOuvrir;
	}




	/**
	 * @return the niveau
	 */
	public int getNiveau() {
		return niveau;
	}




	/**
	 * @param niveau the niveau to set
	 */
	public void setNiveau(int niveau) {
		this.niveau = niveau;
	}




	/**
	 * @return the activite
	 */
	public Object getActivite() {
		return activite;
	}




	/**
	 * @param activite the activite to set
	 */
	public void setActivite(Object activite) {
		this.activite = activite;
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((activite == null) ? 0 : activite.hashCode());
		result = prime * result + ((corps == null) ? 0 : corps.hashCode());
		result = prime * result + (estCree ? 1231 : 1237);
		result = prime * result + (estMotCle ? 1231 : 1237);
		result = prime * result + (estMotCleOuvrir ? 1231 : 1237);
		result = prime * result + niveau;
		result = prime * result + numeroLigne;
		result = prime * result
				+ ((tokentype == null) ? 0 : tokentype.hashCode());
		return result;
	}




	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Jeton other = (Jeton) obj;
		if (activite == null) {
			if (other.activite != null)
				return false;
		} else if (!activite.equals(other.activite))
			return false;
		if (corps == null) {
			if (other.corps != null)
				return false;
		} else if (!corps.equals(other.corps))
			return false;
		if (estCree != other.estCree)
			return false;
		if (estMotCle != other.estMotCle)
			return false;
		if (estMotCleOuvrir != other.estMotCleOuvrir)
			return false;
		if (niveau != other.niveau)
			return false;
		if (numeroLigne != other.numeroLigne)
			return false;
		if (tokentype == null) {
			if (other.tokentype != null)
				return false;
		} else if (!tokentype.equals(other.tokentype))
			return false;
		return true;
	}




	public Jeton(int numeroLigne, String tokentype, boolean estMotCle,
			String corps, boolean estCree, boolean estMotCleOuvrir, int niveau,
			Object activite) {
		super();
		this.numeroLigne = numeroLigne;
		this.tokentype = tokentype;
		this.estMotCle = estMotCle;
		this.corps = corps;
		this.estCree = estCree;
		this.estMotCleOuvrir = estMotCleOuvrir;
		this.niveau = niveau;
		this.activite = activite;
	}




	@Override
	public String toString() {
		// TODO Module de remplacement de méthode auto-généré
		return ("(numeroLigne):"+numeroLigne+"(tokentype:)"+tokentype+"(estMotCle:)"+estMotCle+"(corps:)"+corps+"(estCree:)"+estCree+"(niveau:)"+niveau);
		
	}
	  

	  
}
